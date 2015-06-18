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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.EnumSet;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides output and input streams that read and write to the same array.
 * Automatically increases size as needed.
 *
 * @author jchien
 * @author bkeyes
 */
@ThreadSafe
final class SharedByteArrayStream implements Closeable {

    // from JDK ArrayList implementation
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private enum Mode {
        READ, WRITE
    }

    private final Object lock = new Object();

    private final SharedOutputStream outputStream;
    private final SharedInputStream inputStream;

    private final int windowSize;

    // startPosition is the first byte of the window
    // readPosition is the first byte unread byte
    // writePosition is the first empty element

    // empty when readPosition == writePosition
    // full when (writePosition + 1) % size == startPosition

    // if READ is in modes, the input stream is open
    // if WRITE is in modes, the output stream is open

    @GuardedBy("lock")
    private final EnumSet<Mode> modes = EnumSet.of(Mode.READ, Mode.WRITE);

    @GuardedBy("lock")
    private final byte[] oneByte = new byte[1];

    @GuardedBy("lock")
    private byte[] buffer;

    @GuardedBy("lock")
    private int startPosition = 0;

    @GuardedBy("lock")
    private int readPosition = 0;

    @GuardedBy("lock")
    private int writePosition = 0;

    public SharedByteArrayStream() {
        this(Integer.MAX_VALUE);
    }

    public SharedByteArrayStream(int windowSize) {
        this(windowSize, DEFAULT_BUFFER_SIZE);
    }

    @VisibleForTesting
    SharedByteArrayStream(int windowSize, int bufferSize) {
        checkArgument(windowSize >= 0, "windowSize must be non-negative");
        checkArgument(bufferSize > 0, "bufferSize must be positive");

        this.windowSize = windowSize;
        this.buffer = new byte[bufferSize];

        outputStream = new SharedOutputStream();
        inputStream = new SharedInputStream();
    }

    final class SharedInputStream extends InputStream {

        private SharedInputStream() {}

        @Override
        public int available() throws IOException {
            synchronized (lock) {
                checkOpen(Mode.READ);
                return readSize();
            }
        }

        @Override
        public int read() throws IOException {
            synchronized (lock) {
                checkOpen(Mode.READ);
                if (read(oneByte, 0, 1) == -1) {
                    return -1;
                } else {
                    return oneByte[0];
                }
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            checkArray(b, off, len);
            synchronized (lock) {
                checkOpen(Mode.READ);
                if (len == 0) {
                    return 0;
                }

                if (isMode(Mode.WRITE)) {
                    waitForInput();
                }

                int total = readSize();
                if (total == 0) {
                    return -1;
                } else {
                    return readFromBuffer(b, off, Math.min(len, total));
                }
            }
        }

        @GuardedBy("lock")
        private void waitForInput() throws IOException {
            while (readSize() == 0 && isMode(Mode.READ) && isMode(Mode.WRITE)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException();
                }
            }
        }

        @Override
        public void close() {
            synchronized (lock) {
                modes.remove(Mode.READ);
                lock.notifyAll();
            }
        }
    }

    final class SharedOutputStream extends OutputStream {

        private SharedOutputStream() {}

        @Override
        public void write(int b) throws IOException {
            synchronized (lock) {
                oneByte[0] = (byte) b;
                write(oneByte, 0, 1);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkArray(b, off, len);
            synchronized (lock) {
                checkOpen(Mode.WRITE);
                if (len == 0 || windowSize == 0) {
                    return;
                }

                // truncate writes that exceed the window size
                int length = len;
                int offset = off;
                if (len > windowSize) {
                    offset = off + len - windowSize;
                    length = windowSize;
                }

                if (length > writeSize()) {
                    makeSpace(length);
                }
                writeToBuffer(b, offset, length);

                // discard data now outside of the window
                int bufferedSize = bufferedSize();
                if (bufferedSize > windowSize) {
                    advanceStartPosition(bufferedSize - windowSize);
                }

                lock.notifyAll();
            }
        }

        @Override
        public void close() {
            synchronized (lock) {
                modes.remove(Mode.WRITE);
                lock.notifyAll();
            }
        }
    }

    @GuardedBy("lock")
    private void checkOpen(Mode requiredMode) throws IOException {
        if (!isMode(requiredMode)) {
            throw new IOException("Stream is closed");
        }
    }

    @GuardedBy("lock")
    private boolean isMode(Mode mode) {
        return modes.contains(mode);
    }

    /**
     * Total amount of data currently in the buffer.
     */
    @GuardedBy("lock")
    private int bufferedSize() {
        int count = writePosition - startPosition;
        return (count < 0) ? count + buffer.length : count;
    }

    /**
     * Amount of unread data in the buffer.
     */
    @GuardedBy("lock")
    private int readSize() {
        int count = writePosition - readPosition;
        return (count < 0) ? count + buffer.length : count;
    }

    /**
     * Amount of space available for writing in the buffer.
     */
    @GuardedBy("lock")
    private int writeSize() {
        int count = startPosition - (writePosition + 1);
        return (count < 0) ? count + buffer.length : count;
    }

    @GuardedBy("lock")
    private int readFromBuffer(byte[] b, int off, int len) {
        assert len <= readSize() : "len (" + len + ") > size (" + readSize() + ")";

        copyOut(readPosition, b, off, len);
        advanceReadPosition(len);
        return len;
    }

    @GuardedBy("lock")
    private void writeToBuffer(byte[] b, int off, int len) {
        assert len <= writeSize() : "len (" + len + ") > size (" + writeSize() + ")";
        copyIn(writePosition, b, off, len);
        advanceWritePosition(len);
    }

    @GuardedBy("lock")
    private int copyFromBuffer(byte[] b, int off, int len) {
        assert len <= bufferedSize() : "len (" + len + ") > size (" + bufferedSize() + ")";

        copyOut(startPosition, b, off, len);
        return len;
    }

    /**
     * Copies {@code len} bytes from {@code b} at {@code off} into the buffer at
     * {@code position}.
     */
    @GuardedBy("lock")
    private void copyOut(int position, byte[] b, int off, int len) {
        assert len <= buffer.length : "len (" + len + ") > size (" + buffer.length + ")";
        int total = 0;

        int copyLen = Math.min(len, buffer.length - position);
        System.arraycopy(buffer, position, b, off, copyLen);
        total += copyLen;

        if (total < len) {
            copyLen = len - total;
            System.arraycopy(buffer, 0, b, off + total, copyLen);
            total += copyLen;
        }
    }

    /**
     * Copies {@code len} bytes from the buffer at {@code position} into
     * {@code b} at {@code off}.
     */
    @GuardedBy("lock")
    private void copyIn(int position, byte[] b, int off, int len) {
        assert len <= buffer.length : "len (" + len + ") > size (" + buffer.length + ")";
        int total = 0;

        int copyLen = Math.min(len, buffer.length - position);
        System.arraycopy(b, off, buffer, position, copyLen);
        total += copyLen;

        if (total < len) {
            copyLen = len - total;
            System.arraycopy(b, off + total, buffer, 0, copyLen);
            total += copyLen;
        }
    }

    /**
     * Creates space in the buffer, either by resizing or discarding data.
     */
    @GuardedBy("lock")
    private void makeSpace(int needed) throws IOException {
        assert needed <= windowSize : "need (" + needed + ") > window size (" + windowSize + ")";

        int capacity = buffer.length - 1;
        if (needed < capacity && capacity > windowSize) {
            // the new data should fit if we discard data outside the window
            advanceStartPosition(needed - writeSize());
        } else {
            resize(needed);
        }
    }

    /**
     * Resizes the buffer until at least {@code needed} positions are available.
     */
    @GuardedBy("lock")
    private void resize(int needed) throws IOException {
        int newLength = computeResize(writeSize(), needed, buffer.length);
        if (newLength < 0) {
            throw new IOException("maximum buffer size exceeded");
        }
        byte[] newBuffer = new byte[newLength];

        int readSize = readSize();
        int bufferedSize = bufferedSize();
        copyFromBuffer(newBuffer, 0, bufferedSize);

        buffer = newBuffer;
        startPosition = 0;
        readPosition = bufferedSize - readSize;
        writePosition = bufferedSize;
    }

    @GuardedBy("lock")
    private void advanceStartPosition(int n) {
        // if we will drop unread data, move the read position
        if (isCross(startPosition, readPosition, n, buffer.length)) {
            advanceReadPosition(n);
        }
        startPosition = advance(startPosition, n, buffer.length);
    }

    @GuardedBy("lock")
    private void advanceReadPosition(int n) {
        readPosition = advance(readPosition, n, buffer.length);
    }

    @GuardedBy("lock")
    private void advanceWritePosition(int n) {
        writePosition = advance(writePosition, n, buffer.length);
    }

    private static int advance(int position, int n, int length) {
        return (int) (((long) position + n) % length);
    }

    /**
     * Determines if advancing {@code position} by {@code n} crosses (ends
     * strictly ahead of) {@code fixed}. Assumes {@code position} starts behind
     * or equal with {@code fixed} in the buffer.
     */
    private static boolean isCross(int position, int fixed, int n, int length) {
        if (position < fixed) {
            return n > fixed - position;
        } else if (position > fixed) {
            return (n - (length - position)) > fixed;
        } else {
            return n > 0;
        }
    }

    /**
     * Computes the new size of the buffer during a resize.
     *
     * @param available the amount currently in the buffer
     * @param needed the amount of space needed for the write
     * @param length the current buffer length
     *
     * @return the size of the new buffer or -1 if the maximum size is exceeded
     */
    @VisibleForTesting
    int computeResize(int available, int needed, int length) {
        assert available < needed : "available (" + available + ") >= needed (" + needed + ")";
        assert available < length : "available (" + available + ") >= length (" + length + ")";

        // long to avoid int overflow during computation
        long additional = 0;
        for (int i = 1; additional + available < needed; i++) {
            // double length each iteration (2^i - 1, to account for initial length)
            additional = (long) length * ((1 << i) - 1);
        }

        // min() constrains to int range
        int newLength = (int) Math.min(length + additional, MAX_BUFFER_SIZE);
        if (newLength - (length - available) - 1 < needed) {
            return -1;
        } else {
            return newLength;
        }
    }

    @VisibleForTesting
    int capacity() {
        synchronized (lock) {
            return buffer.length - 1;
        }
    }

    SharedOutputStream getOutputStream() {
        return outputStream;
    }

    SharedInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the buffered data from the input stream as a byte array.
     */
    public byte[] getBufferedData() {
        synchronized (lock) {
            int bufferedSize = bufferedSize();
            byte[] data = new byte[bufferedSize];
            copyFromBuffer(data, 0, bufferedSize);
            return data;
        }
    }

    /**
     * Should not be closed until the remote process has exited.
     */
    @Override
    public void close() {
        synchronized (lock) {
            modes.clear();
            lock.notifyAll();
        }
    }

    private static void checkArray(byte[] b, int off, int len) {
        checkNotNull(b);
        checkElementIndex(off, b.length);
        checkPositionIndex(len, b.length - off);
    }

}
