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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A {@link BaseFileSystemProvider} that caches created file systems, allowing
 * at most one open file system for a given URI at a time. A new file system can
 * be created for a given URI after the previous one is closed.
 *
 * @author bkeyes
 *
 * @param <P> the type of path used by the provider's file systems
 */
public abstract class CachingBaseFileSystemProvider<P extends BasePath<P>> extends
        BaseFileSystemProvider<P> {

    private final SystemCache<Object, BaseFileSystem<P>> cache = new SystemCache<>();

    protected CachingBaseFileSystemProvider(Class<P> pathClass) {
        super(pathClass);
    }

    @Override
    public final FileSystem newFileSystem(final URI uri, final Map<String, ?> env)
            throws IOException {
        Object key = uriToCacheKey(checkUri(uri));
        BaseFileSystem<P> fs = cache.init(key, new SystemCache.Factory<BaseFileSystem<P>>() {
            @Override
            public BaseFileSystem<P> newSystem() throws IOException {
                return openFileSystem(uri, env);
            }
        });

        if (fs != null) {
            return fs;
        } else {
            throw new FileSystemAlreadyExistsException();
        }
    }

    /**
     * Creates a new, open file system. The file system returned by this method
     * is cached until it is closed.
     *
     * @param uri a valid file system URI
     * @param env a possible empty map of provider specific properties
     *
     * @return a new file system
     *
     * @throws IllegalArgumentException if {@code env} does the contain require
     *         properties or if a property value is invalid
     * @throws IOException if an I/O error occurs creating the file system
     */
    protected abstract BaseFileSystem<P> openFileSystem(URI uri, Map<String, ?> env)
            throws IOException;

    @Override
    public final FileSystem getFileSystem(URI uri) {
        Object key = uriToCacheKey(checkUri(uri));
        FileSystem fs = null;
        try {
            fs = cache.get(key);
        } catch (ExecutionException e) {
            // the actual error is reported in the thread calling newFileSystem
            throw new FileSystemNotFoundException(uri.toString() + " (creation error)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileSystemNotFoundException(uri.toString() + " (interruption)");
        }

        if (fs == null) {
            throw new FileSystemNotFoundException(uri.toString());
        } else {
            return fs;
        }
    }

    @Override
    void fileSystemClosed(BaseFileSystem<P> fileSystem) {
        cache.remove(uriToCacheKey(fileSystem.uri()));
    }

    /**
     * Checks that {@code uri} is a valid URI for a file system from this
     * provider.
     *
     * @param uri the URI to check
     *
     * @return the given URI
     *
     * @throws IllegalArgumentException if the URI is invalid
     */
    protected abstract URI checkUri(URI uri);

    /**
     * Converts {@code uri} into a cache key. By default, this method returns
     * the given URI.
     */
    protected Object uriToCacheKey(URI uri) {
        return uri;
    }

}
