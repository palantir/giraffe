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
