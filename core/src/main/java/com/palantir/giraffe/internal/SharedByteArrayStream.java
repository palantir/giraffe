package com.palantir.giraffe.internal;

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

/**
 * Provides output and input streams that read and write to the same array.
 * Automatically increases size as needed.
 *
 * @author jchien
 * @author bkeyes
 */
@ThreadSafe
final class SharedByteArrayStream implements Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private enum Mode {
        READ, WRITE
    }

    private final Object lock = new Object();

    private final SharedOutputStream outputStream;
    private final SharedInputStream inputStream;

    // readPosition is the first byte that can be read
    // writePosition is the first empty element

    // empty when readPosition == writePosition
    // full when (writePosition + 1) % size == readPosition

    // if READ is in modes, the input stream is open
    // if WRITE is in modes, the output stream is open

    @GuardedBy("lock")
    private final EnumSet<Mode> modes = EnumSet.of(Mode.READ, Mode.WRITE);

    @GuardedBy("lock")
    private final byte[] oneByte = new byte[1];

    // The following fields are @GuardedBy("lock")
    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    private int readPosition = 0;
    private int writePosition = 0;

    public SharedByteArrayStream() {
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
                    return copyOut(b, off, Math.min(len, total));
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
                checkOpen(Mode.WRITE);
                oneByte[0] = (byte) b;
                write(oneByte, 0, 1);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkArray(b, off, len);
            synchronized (lock) {
                checkOpen(Mode.WRITE);
                if (len == 0) {
                    return;
                }

                if (len > writeSize()) {
                    resize(len);
                }
                copyIn(b, off, len);
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

    @GuardedBy("lock")
    private int readSize() {
        int count = writePosition - readPosition;
        return (count < 0) ? count + buffer.length : count;
    }

    @GuardedBy("lock")
    private int writeSize() {
        int count = readPosition - (writePosition + 1);
        return (count < 0) ? count + buffer.length : count;
    }

    @GuardedBy("lock")
    private int copyOut(byte[] b, int off, int len) {
        assert len <= readSize() : "len (" + len + ") > size (" + readSize() + ")";
        int total = 0;

        int copyLen = Math.min(len, buffer.length - readPosition);
        System.arraycopy(buffer, readPosition, b, off, copyLen);
        total += copyLen;

        if (total < len) {
            copyLen = len - total;
            System.arraycopy(buffer, 0, b, off + total, copyLen);
            total += copyLen;
        }

        readPosition = (readPosition + total) % buffer.length;
        return len;
    }

    @GuardedBy("lock")
    private int copyIn(byte[] b, int off, int len) {
        assert len <= writeSize() : "len (" + len + ") > size (" + writeSize() + ")";
        int total = 0;

        int copyLen = Math.min(len, buffer.length - writePosition);
        System.arraycopy(b, off, buffer, writePosition, copyLen);
        total += copyLen;

        if (total < len) {
            copyLen = len - total;
            System.arraycopy(b, off + total, buffer, 0, copyLen);
            total += copyLen;
        }

        writePosition = (writePosition + total) % buffer.length;
        return len;
    }

    /**
     * Resizes the backing buffer until at least {@code spaceNeeded} positions
     * are available.
     */
    @GuardedBy("lock")
    private void resize(int spaceNeeded) {
        int multiplier = 2;
        while (writeSize() + (buffer.length * (multiplier - 1)) < spaceNeeded) {
            multiplier *= 2;
        }
        byte[] newBuffer = new byte[buffer.length * multiplier];

        int oldReadSize = readSize();
        copyOut(newBuffer, 0, oldReadSize);
        buffer = newBuffer;
        readPosition = 0;
        writePosition = oldReadSize;
    }

    private static void checkArray(byte[] b, int off, int len) {
        checkNotNull(b);
        checkElementIndex(off, b.length);
        checkPositionIndex(len, b.length - off);
    }

    SharedOutputStream getOutputStream() {
        return outputStream;
    }

    SharedInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Reads the remaining data from the input stream and returns it as a byte array.
     */
    public byte[] readRemainingData() {
        synchronized (lock) {
            int readSize = readSize();
            byte[] data = new byte[readSize];
            copyOut(data, 0, readSize);
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
}
