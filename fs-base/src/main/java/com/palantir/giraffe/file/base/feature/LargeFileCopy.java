package com.palantir.giraffe.file.base.feature;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Indicates that a {@code FileSystemProvider} supports efficient copying of
 * large files.
 *
 * @author alake
 */
public interface LargeFileCopy {

    /**
     * Copies the source file to the target path in an efficient way. An
     * efficient mechanism is one that generally performs better than the
     * default mechanism of streaming between two byte channels. For example, it
     * is often possible to optimize copies between the default file system and
     * a custom file system.
     * <p>
     * At least one of {@code source} or {@code target} will be associated with
     * a {@code FileSystem} from this provider. If this method cannot
     * efficiently copy between the given paths, it throws
     * {@code UnsupportedOperationException}, indicating that the caller should
     * fall back to another mechanism.
     *
     * @param source the path to the file to copy
     * @param target the target path
     *
     * @throws UnsupportedOperationException if this implementation does not
     *         support copies between a given {@code source}-{@code target} pair
     * @throws IOException if an I/O error occurs while copying
     */
    void copyLarge(Path source, Path target) throws IOException;
}
