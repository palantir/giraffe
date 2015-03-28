package com.palantir.giraffe.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

final class StreamCopier implements Callable<Void> {

    private static final int BUFFER_SIZE = 4096;

    private final InputStream source;
    private final OutputStream target;
    private final boolean flushAfterWrite;
    private final byte[] buffer;

    StreamCopier(InputStream source, OutputStream target) {
        this(source, target, false);
    }

    StreamCopier(InputStream source, OutputStream target, boolean flushAfterWrite) {
        this.source = source;
        this.target = target;
        this.flushAfterWrite = flushAfterWrite;

        buffer = new byte[BUFFER_SIZE];
    }

    @Override
    public Void call() throws IOException {
        while (true) {
            int r = source.read(buffer);
            if (r == -1) {
                break;
            }
            target.write(buffer, 0, r);
            if (flushAfterWrite) {
                target.flush();
            }
        }
        return null;
    }

}
