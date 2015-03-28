/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Writes chunks of output to an {@link OutputStream} and can optionally block
 * on a cyclic barrier after each chunk. Used to coordinate read/writes of
 * concurrent scenarios.
 *
 * @author alake
 */
public final class BarrierByteWriter {
    private final ExecutorService es;
    private final OutputStream os;
    private final CyclicBarrier barrier;
    private final Charset encodeCharset;
    private List<ByteWrite> byteWrites;

    /**
     * Private container class that represents a chunk of bytes to write and
     * whether or not to wait on the cyclic barrier after writing.
     *
     * @author alake
     */
    private static final class ByteWrite {
        private byte[] bytes;
        private boolean doBlock;

        private ByteWrite(byte[] bytes, boolean doBlock) {
            this.bytes = bytes;
            this.doBlock = doBlock;
        }
    };

    public BarrierByteWriter(OutputStream senderStream,
                             Charset encodeCharset,
                             CyclicBarrier readWriteBarrier) {
        this.es = Executors.newSingleThreadExecutor();
        this.os = senderStream;
        this.barrier = readWriteBarrier;
        this.encodeCharset = encodeCharset;
        this.byteWrites = new ArrayList<>();
    }

    /*
     * Public Methods
     */

    public BarrierByteWriter writeChunk(byte[] bytes) {
        return writeChunk(bytes, true);
    }

    public BarrierByteWriter writeChunk(byte[] bytes, boolean doBlock) {
        byteWrites.add(new ByteWrite(bytes, doBlock));
        return this;
    }

    public BarrierByteWriter writeChunk(String str) {
        return writeChunk(str, true);
    }

    public BarrierByteWriter writeChunk(String str, boolean doBlock) {
        byteWrites.add(new ByteWrite(str.getBytes(encodeCharset), doBlock));
        return this;
    }

    public void start() {
        // Submit byte writes
        for (ByteWrite bw : byteWrites) {
            es.submit(callableForWrite(bw.bytes, bw.doBlock));
        }

        // Close stream
        es.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                os.close();
                barrier.await();
                return null;
            }
        });

        es.shutdown();
    }

    /*
     * Private Helpers
     */

    private Callable<Void> callableForWrite(final byte[] bytes, final boolean doBlock) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                os.write(bytes, 0, bytes.length);
                os.flush();
                if (doBlock) {
                    barrier.await();
                }
                return null;
            }
        };
    }
}
