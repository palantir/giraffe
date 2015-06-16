package com.palantir.giraffe.file;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * Systems that implement this interface can be converted into
 * {@link FileSystem} instances.
 *
 * @author bkeyes
 */
public interface FileSystemConvertible {

    /**
     * Returns an open {@link FileSystem} that accesses the same resources as
     * this system.
     * <p>
     * The returned system is independent from this system and either can be
     * closed without affecting the other.
     *
     * @throws IOException if an I/O error occurs while creating the new system
     */
    FileSystem asFileSystem() throws IOException;

}
