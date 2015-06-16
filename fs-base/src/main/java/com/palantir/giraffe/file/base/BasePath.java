package com.palantir.giraffe.file.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.UnmodifiableIterator;

/**
 * An abstract {@link Path} implementation that works independent of the file
 * system or path representation.
 * <p>
 * Methods on this class are implemented in terms of other methods defined by
 * {@code Path} or throw {@code UnsupportedOperationException}.
 *
 * @author bkeyes
 *
 * @param <P> the type of the implementing class
 */
public abstract class BasePath<P extends BasePath<P>> implements Path {

    @Override
    public abstract BaseFileSystem<P> getFileSystem();

    @Override
    public boolean startsWith(String other) {
        return startsWith(getFileSystem().getPath(other));
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(getFileSystem().getPath(other));
    }

    @Override
    public Path resolve(String other) {
        return resolve(getFileSystem().getPath(other));
    }

    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(getFileSystem().getPath(other));
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        return new UnmodifiableIterator<Path>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < getNameCount();
            }

            @Override
            public Path next() {
                if (i < getNameCount()) {
                    Path result = getName(i);
                    i++;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return register(watcher, events, new Modifier[0]);
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

}
