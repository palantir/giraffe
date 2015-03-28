package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import com.palantir.giraffe.file.base.attribute.AnnotatedFileAttributeView;
import com.palantir.giraffe.file.base.attribute.DynamicAttributeAccessor;
import com.palantir.giraffe.file.base.attribute.FileAttributeViewFactory;
import com.palantir.giraffe.file.base.attribute.FileAttributeViewRegistry;

/**
 * An abstract {@link FileSystemProvider} implementation that provides common
 * functionality and additional structure for subclasses.
 *
 * @author bkeyes
 *
 * @param <P> the type of path used by the provider's file systems
 */
public abstract class BaseFileSystemProvider<P extends BasePath<P>> extends FileSystemProvider {

    private final Class<P> pathClass;

    protected BaseFileSystemProvider(Class<P> pathClass) {
        this.pathClass = checkNotNull(pathClass, "pathClass must be non-null");
    }

    void fileSystemClosed(BaseFileSystem<P> fileSystem) {
        // nothing to do here
    }

    public final boolean isCompatible(Path p) {
        return pathClass.isInstance(p);
    }

    public final P checkPath(Path p) {
        if (p == null) {
            throw new NullPointerException("path cannot be null");
        } else if (!pathClass.isInstance(p)) {
            String type = p.getClass().getName();
            throw new ProviderMismatchException("incompatible with path of type " + type);
        } else {
            return pathClass.cast(p);
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        P file = checkPath(path);
        file.getFileSystem().delete(file);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        P file = checkPath(path);
        return file.getFileSystem().isHidden(file);
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        P file = checkPath(path);
        file.getFileSystem().checkAccess(file, modes);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        P file = checkPath(path);
        if (file.equals(path2)) {
            return true;
        } else if (!isCompatible(path2)) {
            return false;
        } else {
            return file.getFileSystem().isSameFile(file, checkPath(path2));
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
            LinkOption... options) {
        FileAttributeViewFactory<?> factory = getViewRegistry(path).getByViewType(type);
        return type.cast(factory.newView(path, options));
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        P file = checkPath(path);
        return file.getFileSystem().newByteChannel(file, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter)
            throws IOException {
        P path = checkPath(dir);
        return path.getFileSystem().newDirectoryStream(path, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        P path = checkPath(dir);
        path.getFileSystem().createDirectory(path, attrs);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type,
            LinkOption... options) throws IOException {
        FileAttributeViewFactory<?> factory = getViewRegistry(path).getByAttributesType(type);
        return type.cast(factory.newView(path, options).readAttributes());
    }

    @Override
    public final Map<String, Object> readAttributes(Path path, String viewAndAttributes,
            LinkOption... options) throws IOException {
        String[] parts = parseAttributeSpec(viewAndAttributes);

        FileAttributeViewFactory<?> factory = getViewRegistry(path).getByViewName(parts[0]);
        AnnotatedFileAttributeView view = factory.newView(path, options);
        return new DynamicAttributeAccessor(view).readAttributes(parts[1]);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
            throws IOException {
        String[] parts = parseAttributeSpec(attribute);

        FileAttributeViewFactory<?> factory = getViewRegistry(path).getByViewName(parts[0]);
        AnnotatedFileAttributeView view = factory.newView(path, options);
        new DynamicAttributeAccessor(view).setAttribute(parts[1], value);
    }

    private FileAttributeViewRegistry getViewRegistry(Path path) {
        return checkPath(path).getFileSystem().fileAttributeViews();
    }

    /**
     * Parses a {@code [view:]attributes} string into an two-element array. The first
     * element is the view, or "basic" if the view is not included. The second
     * element is the comma-separated attribute list.
     *
     * @throws IllegalArgumentException if the attributes list is empty
     */
    private static String[] parseAttributeSpec(String viewAndAttributes) {
        String view = "basic";
        String attributes = viewAndAttributes;

        int split = viewAndAttributes.indexOf(':');
        if (split >= 0) {
            view = viewAndAttributes.substring(0, split);
            attributes = viewAndAttributes.substring(split + 1);
        }
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("no attributes specified");
        }
        return new String[] { view, attributes };
    }
}
