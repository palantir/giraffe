package com.palantir.giraffe.file.base.feature;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Indicates that a {@code FileSystemProvider} supports efficient recursive
 * deletes.
 *
 * @author bkeyes
 */
public interface RecursiveDelete {

    /**
     * Deletes the given path or directory recursively.
     *
     * @param path the path to the file or directory
     *
     * @throws NoSuchFileException if the path does not exist
     * @throws IOException if an I/O error occurs while deleting the path
     */
    void deleteRecursive(Path path) throws IOException;

    /**
     * Deletes the given path or directory recursively, if it exists.
     *
     * @param path the path to the file or directory
     *
     * @return {@code true} if path existed and was deleted, {@code false} if
     *         the path could not be deleted because it did not exist
     *
     * @throws IOException if an I/O error occurs while deleting the path
     */
    boolean deleteRecursiveIfExists(Path path) throws IOException;
}
