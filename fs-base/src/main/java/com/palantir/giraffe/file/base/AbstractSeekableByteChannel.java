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
package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract base classe for {@link SeekableByteChannel} implementations.
 * <p>
 * This class makes {@code close()} idempotent and provides utilities for
 * managing the channel's position. All I/O operations are left to subclasses.
 *
 * @author bkeyes
 */
// TODO(bkeyes): does this need to be thread-safe or atomic (e.g. between write
// and truncate)?
public abstract class AbstractSeekableByteChannel implements SeekableByteChannel {

    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * This channel's position. While implementations may access this directly,
     * they are encouraged to use {@link #advancePosition(int)} and
     * {@link #truncatePosition(long)} when possible. This is exposed primarily
     * for efficiency when reading the position or setting it to an exact value.
     */
    protected long position = 0;

    @Override
    public final boolean isOpen() {
        return !closed.get();
    }

    @Override
    public final void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    /**
     * Performs implementation-specific close actions. This method is called at
     * most once.
     *
     * @throws IOException if an I/O error occurs while closing this channel.
     */
    protected abstract void doClose() throws IOException;

    @Override
    public long position() throws IOException {
        checkIsOpen();
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        checkArgument(newPosition >= 0, "position cannot be negative");
        checkIsOpen();

        position = newPosition;
        return this;
    }

    /**
     * Advances this channel's position by the given number of bytes, if it is
     * positive. If the number of bytes is zero or negative, the position is not
     * changed.
     *
     * @param numBytes the number of bytes transferred
     *
     * @return the input number of bytes
     */
    protected final int advancePosition(int numBytes) {
        if (numBytes > 0) {
            position += numBytes;
        }
        return numBytes;
    }

    /**
     * Sets this channel's position to {@code size} if it is less than the
     * current position.
     *
     * @param size the new size of the underlying resource
     *
     * @return {@code true} if this channel's position changed, {@code false}
     *         otherwise
     */
    protected final boolean truncatePosition(long size) {
        checkArgument(size >= 0, "size cannot be negative");
        if (size < position) {
            position = size;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks that this channel is open. Implementations should call this at the
     * start of every I/O method.
     *
     * @throws ClosedChannelException if this channel is closed
     */
    protected final void checkIsOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

}
