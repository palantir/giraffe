package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.Ordering;

/**
 * An ordered list of {@link Closeable}s that is closed in order. Registered
 * closeables are strongly-referenced and should be unregistered if they are
 * closed before the registry is closed.
 * <p>
 * The list is ordered by ascending priority; closeables with priority closer to
 * {@code Integer.MIN_VALUE} are closed sooner.
 *
 * @author bkeyes
 */
public final class CloseableRegistry implements Closeable {

    private static final int DEFAULT_PRIORITY = 0;

    @GuardedBy("this")
    private Set<PriorityCloseable> closeables = new HashSet<>();

    /**
     * Registers {@code closeable} with the default priority, {@code 0}.
     *
     * @return {@code true} if the closeable was registered or {@code false} if
     *         the registry is closed
     */
    public boolean register(Closeable closeable) {
        return register(closeable, DEFAULT_PRIORITY);
    }

    /**
     * Registers {@code closeable} with the given priority.
     *
     * @return {@code true} if the closeable was registered or {@code false} if
     *         the registry is closed
     */
    public synchronized boolean register(Closeable closeable, int priority) {
        if (closeables == null) {
            return false;
        } else {
            checkNotNull(closeable, "closeable must be non-null");
            closeables.add(new PriorityCloseable(closeable, priority));
            return true;
        }
    }

    /**
     * Removes {@code closeable} from this registry.
     *
     * @return {@code true} if the closeable was removed or {@code false} if the
     *         registry is closed or the closeable was not registered.
     */
    public synchronized boolean unregister(Closeable closeable) {
        if (closeables != null) {
            // priority is not used for equality
            return closeables.remove(new PriorityCloseable(closeable, DEFAULT_PRIORITY));
        } else {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        Set<PriorityCloseable> toClose = null;
        synchronized (this) {
            // transfer ownership to the thread performing the close,
            // eliminating the need to hold a lock while closing objects
            toClose = closeables;
            closeables = null;
        }

        if (toClose != null) {
            // only executed by the first thread to call close()
            closeInOrder(toClose);
        }
    }

    private static void closeInOrder(Set<PriorityCloseable> toClose) throws IOException {
        SuppressedCloseable.create(Ordering.natural().immutableSortedCopy(toClose)).close();
    }

    private static final class PriorityCloseable implements Closeable,
            Comparable<PriorityCloseable> {

        private final Closeable closeable;
        private final int priority;

        PriorityCloseable(Closeable closeable, int priority) {
            this.closeable = closeable;
            this.priority = priority;
        }

        @Override
        public void close() throws IOException {
            closeable.close();
        }

        @Override
        public int compareTo(PriorityCloseable other) {
            return Integer.compare(priority, other.priority);
        }

        @Override
        public int hashCode() {
            return closeable.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PriorityCloseable) {
                return closeable.equals(((PriorityCloseable) obj).closeable);
            }
            return false;
        }
    }
}
