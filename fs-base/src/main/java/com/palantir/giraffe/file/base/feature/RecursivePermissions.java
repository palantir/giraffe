package com.palantir.giraffe.file.base.feature;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import com.palantir.giraffe.file.base.attribute.PermissionChange;

/**
 * Indicates that a {@code FileSystemProvider} supports efficient recursive
 * permission changes.
 *
 * @author bkeyes
 */
public interface RecursivePermissions {

    /**
     * Recursively changes permissions on the given path.
     *
     * @param path the path to the file or directory
     * @param change the type of permission change to make
     * @param permissions the permissions to modify
     *
     * @throws IOException if an I/O error occurs while changing permissions
     */
    void changePermissionsRecursive(Path path, PermissionChange change,
            Set<PosixFilePermission> permissions) throws IOException;

}
