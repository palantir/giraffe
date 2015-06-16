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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;

/**
 * An abstract {@link Path} implementation backed by an {@link ImmutableList} of
 * segments.
 * <p>
 * This class implements most path operation that function independently of the
 * file system. Subclasses must implement file system specific operation, like
 * {@link #toUri() toUri} and {@link #toRealPath(java.nio.file.LinkOption...)
 * toRealPath}. Subclasses may also override other methods on the class to
 * change functionality or implement features more efficiently.
 *
 * @author bkeyes
 *
 * @param <P> the type of the implementing class
 */
public abstract class AbstractImmutableListPath<P extends AbstractImmutableListPath<P>> extends
        BasePath<P> {

    private final ImmutableListPathCore core;

    protected AbstractImmutableListPath(ImmutableListPathCore core) {
        this.core = checkNotNull(core, "core must be non-null");
    }

    @Override
    public boolean isAbsolute() {
        return core.isAbsolute();
    }

    @Override
    public Path getRoot() {
        ImmutableListPathCore root = core.getRoot();
        if (root == null) {
            return null;
        } else {
            return newPath(root);
        }
    }

    @Override
    public Path getParent() {
        ImmutableListPathCore parent = core.getParent();
        if (parent == null) {
            return null;
        } else {
            return newPath(parent);
        }
    }

    @Override
    public Path getFileName() {
        ImmutableListPathCore name = core.getFileName();
        if (name == null) {
            return null;
        } else {
            return newPath(name);
        }
    }

    @Override
    public int getNameCount() {
        return core.getNameCount();
    }

    @Override
    public Path getName(int index) {
        ImmutableListPathCore name = core.getName(index);
        if (name == null) {
            return null;
        } else {
            return newPath(name);
        }
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return newPath(core.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        if (!getFileSystem().provider().isCompatible(other)) {
            return false;
        } else {
            return core.startsWith(checkPath(other).getCore());
        }
    }

    @Override
    public boolean endsWith(Path other) {
        if (!getFileSystem().provider().isCompatible(other)) {
            return false;
        } else {
            return core.endsWith(checkPath(other).getCore());
        }
    }

    @Override
    public Path normalize() {
        return newPath(core.normalize());
    }

    @Override
    public Path resolve(Path other) {
        return newPath(core.resolve(checkPath(other).getCore()));
    }

    @Override
    public Path resolveSibling(Path other) {
        return newPath(core.resolveSibling(checkPath(other).getCore()));
    }

    @Override
    public Path relativize(Path other) {
        return newPath(core.relativize(checkPath(other).getCore()));
    }

    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        } else {
            return getFileSystem().defaultDirectory().resolve(this);
        }
    }

    @Override
    public URI toUri() {
        return getFileSystem().uri().resolve(toAbsolutePath().toString());
    }

    @Override
    public final String toString() {
        String separator = getFileSystem().getSeparator();
        return core.toPathString(separator);
    }

    @Override
    public int compareTo(Path p) {
        return core.compareTo(checkPath(p).getCore());
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    private P checkPath(Path p) {
        return getFileSystem().provider().checkPath(p);
    }

    protected final ImmutableListPathCore getCore() {
        return core;
    }

    protected abstract P newPath(ImmutableListPathCore newCore);

}
