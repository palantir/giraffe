package com.palantir.giraffe.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A temporary path that is deleted when closed.
 *
 * @author bkeyes
 */
public final class TempPath implements AutoCloseable {

    private static final String PREFIX = "autodelete";
    private static final String SUFFIX = ".tmp";

    /**
     * Creates a new temporary directory in the default temporary file location.
     *
     * @throws IOException if an I/O error occurs while creating the directory
     */
    public static TempPath createDirectory() throws IOException {
        return wrap(Files.createTempDirectory(PREFIX));
    }

    /**
     * Creates a new temporary directory in the specified directory.
     *
     * @throws IOException if an I/O error occurs while creating the directory
     */
    public static TempPath createDirectory(Path dir) throws IOException {
        return wrap(Files.createTempDirectory(dir, PREFIX));
    }

    /**
     * Creates a new temporary file in the default temporary file location.
     *
     * @throws IOException if an I/O error occurs while creating the file
     */
    public static TempPath createFile() throws IOException {
        return wrap(Files.createTempFile(PREFIX, SUFFIX));
    }

    /**
     * Creates a new temporary file in the specified directory.
     *
     * @throws IOException if an I/O error occurs while creating the file
     */
    public static TempPath createFile(Path dir) throws IOException {
        return wrap(Files.createTempFile(dir, PREFIX, SUFFIX));
    }

    /**
     * Wraps an existing {@link Path} as a {@code TempPath}. To allow reasoning
     * about deletion, <strong>all</strong> access to the delegate path should
     * use the returned wrapper.
     *
     * @param path the path to wrap
     */
    public static TempPath wrap(Path path) {
        return new TempPath(path);
    }

    private final AtomicBoolean closed = new AtomicBoolean();
    private final Path path;

    private TempPath(Path path) {
        this.path = path;
    }

    /**
     * Returns the {@link Path} to the temporary file or directory.
     * <p>
     * Clients must take care to avoid leaking the returned reference outside
     * the scope of a surrounding try-with-resources block, as the path is
     * deleted when this object is closed.
     *
     * @throws IllegalStateException if this path is closed
     */
    public Path path() {
        if (closed.get()) {
            throw new IllegalStateException("path is closed");
        }
        return path;
    }

    /**
     * Closes this path, deleting the file or directory if it exists. If this
     * path is already closed, this method has no effect.
     *
     * @throws IOException if an I/O error occurs while deleting this path
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            if (Files.exists(path)) {
                MoreFiles.deleteRecursive(path);
            }
        }
    }

    @Override
    public String toString() {
        return "TempPath[" + path.toString() + "]";
    }

}
