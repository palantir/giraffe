/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.giraffe.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.palantir.giraffe.internal.SharedByteArrayStream.SharedInputStream;
import com.palantir.giraffe.internal.SharedByteArrayStream.SharedOutputStream;

/**
 * Tests thread-safety of {@link SharedByteArrayStream}.
 *
 * @author bkeyes
 */
public class SharedByteArrayStreamConcurrencyTest {

    /**
     * Number of test iterations.
     */
    private static final int ITERATIONS = 15;

    /**
     * Amount of data to read/write in each iteration.
     */
    private static final int DATA_SIZE = 8192;

    /**
     * Number of chunks to split data into for writing.
     */
    private static final int DATA_CHUNKS = 64;

    private Random rand;

    private ExecutorService executor;
    private CyclicBarrier barrier;

    @Before
    public void setup() {
        rand = new Random();

        executor = Executors.newFixedThreadPool(2);
        barrier = new CyclicBarrier(2);
    }

    @After
    public void teardown() {
        executor.shutdown();
    }

    @Test
    public void concurrentReadAndWrite() throws InterruptedException {
        for (int i = 0; i < ITERATIONS; i++) {
            SharedByteArrayStream stream = new SharedByteArrayStream();
            SharedInputStream is = stream.getInputStream();
            SharedOutputStream os = stream.getOutputStream();

            Future<byte[]> writeFuture = executor.submit(new WriteAction(os));
            Future<byte[]> readFuture = executor.submit(new ReadAction(is));

            byte[] expected = null;
            try {
                expected = writeFuture.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw new AssertionError("unexpected exception", e.getCause());
            } catch (TimeoutException e) {
                fail("timeout waiting for write action");
            }

            byte[] actual = null;
            try {
                actual = readFuture.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw new AssertionError("unexpected exception", e.getCause());
            } catch (TimeoutException e) {
                fail("timeout waiting for read action");
            }

            assertArrayEquals("incorrect data", expected, actual);
        }
    }

    private final class WriteAction implements Callable<byte[]> {
        private final OutputStream os;
        private final byte[] data;
        private final int[] chunkSizes;

        private int index = 0;

        WriteAction(OutputStream os) {
            this.os = os;
            data = new byte[DATA_SIZE];
            rand.nextBytes(data);

            chunkSizes = getChunkSizes();
        }

        @Override
        public byte[] call() throws Exception {
            barrier.await();

            for (int i = 0; i < chunkSizes.length; i++) {
                os.write(data, index, chunkSizes[i]);
                index += chunkSizes[i];

                if (rand.nextDouble() < 0.40) {
                    Thread.sleep(1);
                }
            }
            os.close();

            return data;
        }
    }

    private final class ReadAction implements Callable<byte[]> {
        private final InputStream is;
        private final byte[] data;

        private int index = 0;

        public ReadAction(InputStream is) {
            this.is = is;
            data = new byte[DATA_SIZE];
        }

        @Override
        public byte[] call() throws Exception {
            barrier.await();

            while (index < DATA_SIZE) {
                int r = is.read(data, index, DATA_SIZE - index);
                if (r == -1) {
                    break;
                }
                index += r;

                if (rand.nextDouble() < 0.40) {
                    Thread.sleep(1);
                }
            }
            is.close();

            return data;
        }
    }

    private int[] getChunkSizes() {
        int total = 0;
        int[] sizes = new int[DATA_CHUNKS];

        for (int i = 0; i < sizes.length - 1; i++) {
            int remaining = sizes.length - i;

            int size = 1;
            if (remaining < DATA_SIZE - total) {
                double mean = ((double) DATA_SIZE - total) / remaining;
                double stdDev = mean / 4;

                size = (int) Math.round(rand.nextGaussian() * stdDev + mean);
                size = Math.max(1, size);
                if (total + size > DATA_SIZE) {
                    size = DATA_SIZE - total - remaining;
                }
            }

            sizes[i] = size;
            total += size;
        }
        sizes[sizes.length - 1] = DATA_SIZE - total;

        return sizes;
    }
}
