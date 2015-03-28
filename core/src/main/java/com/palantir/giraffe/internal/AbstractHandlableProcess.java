package com.palantir.giraffe.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import com.palantir.giraffe.file.base.SuppressedCloseable;

/**
 * An abstract base implementation of {@link HandlableProcess} that implements
 * stream closing.
 *
 * @author bkeyes
 */
public abstract class AbstractHandlableProcess implements HandlableProcess {

    @Override
    public final void closeStreams() throws IOException {
        closeSuppressed(getOutput(), getError(), getInput());
    }

    /**
     * Closes the given {@code Closeable}s in order, suppressing intermediate
     * exceptions and throwing the most recent exception thrown by a
     * {@code close()} method, if any.
     */
    protected static void closeSuppressed(Closeable... closeables) throws IOException {
        SuppressedCloseable.create(Arrays.asList(closeables)).close();
    }

}
