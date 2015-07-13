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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.palantir.giraffe.file.base.attribute.FileAttributeViewRegistry;

/**
 * An abstract {@link FileSystem} implementation that provides common
 * functionality and additional structure for subclasses.
 *
 * @author bkeyes
 *
 * @param <P> the type of path used by the file system
 */
public abstract class BaseFileSystem<P extends BasePath<P>> extends FileSystem {

    @Override
    public abstract BaseFileSystemProvider<P> provider();

    /**
     * Returns the default directory of this file system. The default directory
     * is the directory against which all relative paths are resolved.
     */
    public abstract P defaultDirectory();

    /**
     * Returns this file system's URI. Passing this URI to
     * {@link java.nio.file.FileSystems#getFileSystem(URI) getFileSystem} should
     * return this file system instance.
     */
    public abstract URI uri();

    @Override
    public P getPath(String first, String... more) {
        if (more.length == 0) {
            return getPath(first);
        } else {
            return getPath(Joiner.on(getSeparator()).join(Lists.asList(first, more)));
        }
    }

    /**
     * Converts a single path string with separators to a {@link Path} object.
     *
     * @see #getPath(String, String...)
     */
    protected abstract P getPath(String path);

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return StandardPathMatchers.fromSyntaxAndPattern(syntaxAndPattern, getSeparator());
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Set<String> supportedFileAttributeViews() {
        return fileAttributeViews().getRegisteredViews();
    }

    public abstract FileAttributeViewRegistry fileAttributeViews();

    //
    // File Operations
    //

    protected abstract void delete(P path) throws IOException;

    protected abstract boolean isHidden(P path) throws IOException;

    protected abstract void checkAccess(P path, AccessMode... modes) throws IOException;

    /**
     * Determines two non-equal paths refer to the same file. While {@code path}
     * is always associated with this file system, {@code other} may be
     * associated with a different file system from the same provider.
     */
    protected abstract boolean isSameFile(P path, P other) throws IOException;

    protected abstract void createDirectory(P path, FileAttribute<?>... attrs) throws IOException;

    protected abstract SeekableByteChannel newByteChannel(P path,
            Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

    protected abstract DirectoryStream<Path> newDirectoryStream(P dir,
            Filter<? super Path> filter) throws IOException;

}
