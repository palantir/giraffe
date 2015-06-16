package com.palantir.giraffe.file.base;

import java.io.Closeable;
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
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean closed = new AtomicBoolean();
    private final CloseableRegistry closeableRegistry = new CloseableRegistry();

    @Override
    public abstract BaseFileSystemProvider<P> provider();

    @Override
    public final void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            try {
                closeableRegistry.close();
                doClose();
            } finally {
                provider().fileSystemClosed(this);
            }
        }
    }

    /**
     * Performs any implementation-specific close actions. By default, this
     * method does nothing. Any registered resources are closed before this
     * method is called.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void doClose() throws IOException {
        // do nothing by default
    }

    /**
     * Registers a {@link Closeable} resource with the default priority of
     * {@code 0}.
     */
    public <C extends Closeable> C registerCloseable(C closeable) {
        closeableRegistry.register(closeable);
        return closeable;
    }

    /**
     * Registers a {@link Closeable} resource with the given priority.
     */
    public <C extends Closeable> C registerCloseable(C closeable, int priority) {
        closeableRegistry.register(closeable, priority);
        return closeable;
    }

    /**
     * Unregisters a {@link Closeable} resource, usually after it has been
     * closed.
     */
    public void unregisterCloseable(Closeable closeable) {
        closeableRegistry.unregister(closeable);
    }

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
    public final boolean isOpen() {
        return !closed.get();
    }

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
