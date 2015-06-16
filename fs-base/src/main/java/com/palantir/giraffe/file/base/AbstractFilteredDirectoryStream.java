package com.palantir.giraffe.file.base;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * An thread-safe, abstract {@link DirectoryStream} that applies a filter to an
 * underlying entry iterator.
 *
 * @author bkeyes
 *
 * @param <T> the type of element returned by the iterator
 */
public abstract class AbstractFilteredDirectoryStream<T> implements DirectoryStream<T> {

    private static enum State {
        INITIAL, CREATED, CLOSED;
    }

    private final Filter<? super T> filter;
    private final AtomicReference<State> state = new AtomicReference<>(State.INITIAL);

    protected AbstractFilteredDirectoryStream(Filter<? super T> filter) {
        this.filter = filter;
    }

    @Override
    public final void close() throws IOException {
        if (state.getAndSet(State.CLOSED) != State.CLOSED) {
            doClose();
        }
    }

    /**
     * Performs implementation-specific close actions. This method is called
     * at most once.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doClose() throws IOException;

    @Override
    public final Iterator<T> iterator() {
        if (state.compareAndSet(State.INITIAL, State.CREATED)) {
            return newThreadSafeFilteredIterator();
        } else {
            // state may no longer have the same value that caused compareAndSet
            // to fail, but since an exception is thrown in either case, this
            // race is acceptable
            State current = state.get();
            if (current == State.CREATED) {
                throw new IllegalStateException("iterator already created");
            } else {
                throw new IllegalStateException("stream is closed");
            }
        }
    }

    /**
     * Creates and returns an iterator over the entries in this directory. This
     * method is called at most once and only if the stream is open.
     * <p>
     * The iterator is not required to be thread-safe, but otherwise must follow
     * the read-ahead contract outlined by {@link DirectoryStream}.
     */
    protected abstract Iterator<T> entryIterator();

    private Iterator<T> newThreadSafeFilteredIterator() {
        final Iterator<T> delegate = newFilteredIterator();
        return new UnmodifiableIterator<T>() {
            @Override
            public boolean hasNext() {
                synchronized (delegate) {
                    return delegate.hasNext();
                }
            }

            @Override
            public T next() {
                synchronized (delegate) {
                    return delegate.next();
                }
            }
        };
    }

    private Iterator<T> newFilteredIterator() {
        return Iterators.filter(entryIterator(), new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                try {
                    return filter.accept(input);
                } catch (IOException e) {
                    throw new DirectoryIteratorException(e);
                }
            }
        });
    }

}
