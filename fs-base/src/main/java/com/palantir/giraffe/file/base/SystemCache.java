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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.AbstractFuture;

/**
 * A thread-safe cache for file systems or similar I/O systems. Systems are
 * initialized and cached atomically. Threads that try to get a system while it
 * is being constructed block until construction is complete or an error occurs.
 *
 * @author bkeyes
 *
 * @param <K> the type of cache keys
 * @param <S> the type of cached systems
 */
public final class SystemCache<K, S> {

    /**
     * Constructs new systems to be stored in the cache.
     *
     * @param <S> the type of system to construct
     */
    public interface Factory<S> {
        S newSystem() throws IOException;
    }

    private static final class Reference<S> extends AbstractFuture<S> {
        private void init(S system) {
            set(system);
        }

        private void fail(Throwable t) {
            setException(t);
        }
    }

    private final ConcurrentHashMap<K, Reference<S>> cache = new ConcurrentHashMap<>();

    /**
     * Gets the system associated with {@code key} if it exists. Returns
     * {@code null} if there is no system associated with {@code key}.
     * <p>
     * If a thread calls this method concurrently with another thread that is
     * initializing a system for the same key, this thread will block until
     * initialization completes. If an error occurs in the initialization
     * thread, it is also reported to this thread.
     *
     * @throws InterruptedException if this thread is interrupted while waiting
     *         for initialization
     * @throws ExecutionException if an error occurs while this thread is
     *         waiting for initialization
     */
    public S get(K key) throws InterruptedException, ExecutionException {
        Reference<S> ref = cache.get(key);
        if (ref == null) {
            return null;
        } else {
            return ref.get();
        }
    }

    /**
     * Atomically initializes a system associated with {@code key} if no such
     * system exists. If this thread initialized the system, the system is
     * returned. Otherwise, this method returns {@code null}.
     *
     * @throws IOException if an I/O error occurs while initializing the system
     */
    public S init(K key, Factory<S> factory) throws IOException {
        final Reference<S> existing = cache.get(key);
        if (existing == null) {
            Reference<S> ref = new Reference<>();
            if (cache.putIfAbsent(key, ref) == null) {
                try {
                    S system = factory.newSystem();
                    ref.init(system);
                    return system;
                } catch (IOException e) {
                    ref.fail(e);
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * Removes the initialized system associated with {@code key} from the
     * cache.
     *
     * @throws IllegalStateException if this method is called while
     *         initialization is in progress.
     */
    public void remove(K key) {
        Reference<S> ref = cache.get(key);
        if (ref.isDone()) {
            cache.remove(key, ref);
        } else {
            throw new IllegalStateException("system is not initialized, key = " + key);
        }
    }
}
