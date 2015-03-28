package com.palantir.giraffe.file.base;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A composite {@link Closeable} that closes the given {@code Closeable}s in
 * order, suppressing intermediate exceptions.
 *
 * @author bkeyes
 */
// TODO(bkeyes): move this to a more general package
public class SuppressedCloseable implements Closeable {

    public static SuppressedCloseable create(Closeable first, Closeable... others) {
        return new SuppressedCloseable(Lists.asList(first, others));
    }

    public static SuppressedCloseable create(List<? extends Closeable> closeables) {
        return new SuppressedCloseable(closeables);
    }

    private final List<? extends Closeable> closeables;

    private SuppressedCloseable(List<? extends Closeable> closeables) {
        this.closeables = closeables;
    }

    /**
     * Closes the contained {@code Closeable}s in order. If any {@code close()}
     * method throws an {@code IOException}, it is caught and the first such
     * exception suppresses any future exceptions.
     */
    @Override
    public void close() throws IOException {
        IOException exception = null;
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
