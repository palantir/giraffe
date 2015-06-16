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
package com.palantir.giraffe;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.command.ExecutionSystemConvertible;
import com.palantir.giraffe.command.ExecutionSystems;
import com.palantir.giraffe.file.FileSystemConvertible;

/**
 * Creates {@link FileSystem} or {@link ExecutionSystem} instances from systems
 * of the other type.
 * <p>
 * Converting one system to another does not affect the original system. The new
 * system is independent and closing either system does not close the other.
 * Where possible, the new system minimizes resource duplication, reusing
 * connections or other state that is safely shared.
 * <p>
 * Not all file systems have a corresponding execution system and not all
 * execution systems have a corresponding file system. If a conversion is not
 * possible, methods in this class throw {@code UnsupportedOperationException}.
 * Conversion between the default systems of each type is always supported. See
 * provider-specific documentation for details about the compatibility of other
 * system implementations.
 *
 * @author bkeyes
 *
 * @see FileSystemConvertible
 * @see ExecutionSystemConvertible
 */
public class SystemConverter {

    /**
     * Returns an open {@link FileSystem} that accesses the same resources as
     * the given command's execution system.
     *
     * @param command the command
     *
     * @throws IOException if an I/O error occurs while creating the
     *         {@code FileSystem}
     *
     * @see #asFileSystem(ExecutionSystem)
     */
    public static FileSystem asFileSystem(Command command) throws IOException {
        checkNotNull(command, "command must be non-null");
        return asFileSystem(command.getExecutionSystem());
    }

    /**
     * Returns an open {@link FileSystem} that accesses the same resources as
     * the given execution system. The execution system is not modified.
     * <p>
     * The file system can read and modify the same files that are accessible to
     * commands executed by the execution system. Changes made by one system are
     * visible to the other, given certain system-dependent conditions are true.
     * In particular, no guarantees are made for concurrent modification or
     * modifications from different threads.
     *
     * @param es the execution system
     *
     * @return an open {@code FileSystem}
     *
     * @throws IOException if an I/O error occurs while creating the
     *         {@code FileSystem}
     * @throws UnsupportedOperationException if the given execution system does
     *         not support file system conversion
     */
    public static FileSystem asFileSystem(ExecutionSystem es) throws IOException {
        checkNotNull(es, "system must be non-null");
        if (es.equals(ExecutionSystems.getDefault())) {
            return FileSystems.getDefault();
        } else if (es instanceof FileSystemConvertible) {
            return ((FileSystemConvertible) es).asFileSystem();
        } else {
            throw uncovertableError(es.getClass());
        }
    }

    /**
     * Returns an open {@link ExecutionSystem} that accesses the same resources
     * as the given path's file system.
     *
     * @param path the path
     *
     * @throws IOException if an I/O error occurs while creating the
     *         {@code ExecutionSystem}
     *
     * @see #asExecutionSystem(FileSystem)
     */
    public static ExecutionSystem asExecutionSystem(Path path) throws IOException {
        checkNotNull(path, "path must be non-null");
        return asExecutionSystem(path.getFileSystem());
    }

    /**
     * Returns an open {@link ExecutionSystem} that accesses the same resources
     * as the given file system. The file system is not modified.
     * <p>
     * Commands executed by the execution system can read and modify the same
     * files that are accessible by the file system. Changes made by one system
     * are visible to the other, given certain system-dependent conditions are
     * true. In particular, no guarantees are made for concurrent modification
     * or modifications from different threads.
     *
     * @param fs the file system
     *
     * @return an open {@code ExecutionSystem}
     *
     * @throws IOException if an I/O error occurs while creating the
     *         {@code ExecutionSystem}
     * @throws UnsupportedOperationException if the given file system does not
     *         support execution system conversion
     */
    public static ExecutionSystem asExecutionSystem(FileSystem fs) throws IOException {
        checkNotNull(fs, "system must be non-null");
        if (fs.equals(FileSystems.getDefault())) {
            return ExecutionSystems.getDefault();
        } else if (fs instanceof ExecutionSystemConvertible) {
            return ((ExecutionSystemConvertible) fs).asExecutionSystem();
        } else {
            throw uncovertableError(fs.getClass());
        }
    }

    /**
     * Returns {@code true} if the given file system can be converted into an
     * execution system.
     *
     * @param fs the file system
     */
    public static boolean isConvertible(FileSystem fs) {
        return FileSystems.getDefault().equals(fs) || fs instanceof ExecutionSystemConvertible;
    }

    /**
     * Returns {@code true} if the given execution system can be converted into
     * a file system.
     *
     * @param es the execution system
     */
    public static boolean isConvertible(ExecutionSystem es) {
        return ExecutionSystems.getDefault().equals(es) || es instanceof FileSystemConvertible;
    }

    private static UnsupportedOperationException uncovertableError(Class<?> systemClass) {
        String msg = systemClass.getName() + " is not convertible";
        throw new UnsupportedOperationException(msg);
    }

    private SystemConverter() {
        throw new UnsupportedOperationException();
    }
}
