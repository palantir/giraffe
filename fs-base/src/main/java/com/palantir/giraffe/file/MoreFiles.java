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
package com.palantir.giraffe.file;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.palantir.giraffe.file.base.attribute.PermissionChange;
import com.palantir.giraffe.file.base.feature.LargeFileCopy;
import com.palantir.giraffe.file.base.feature.RecursiveCopy;
import com.palantir.giraffe.file.base.feature.RecursiveDelete;
import com.palantir.giraffe.file.base.feature.RecursivePermissions;

/**
 * Provides static methods that extend the functionality provided by
 * {@link Files}.
 *
 * @author bkeyes
 */
public final class MoreFiles {

    private static final DirectoryStream.Filter<Path> FILE_FILTER =
        new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isRegularFile(entry);
            }
        };

    private static final DirectoryStream.Filter<Path> DIRECTORY_FILTER =
        new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isDirectory(entry);
            }
        };

    /**
     * Returns a directory filter that matches {@linkplain Files#isRegularFile
     * regular} files.
     */
    public static DirectoryStream.Filter<Path> regularFileFilter() {
        return FILE_FILTER;
    }

    /**
     * Returns a directory filter that matches {@linkplain Files#isDirectory
     * directories}.
     */
    public static DirectoryStream.Filter<Path> directoryFilter() {
        return DIRECTORY_FILTER;
    }

    /**
     * Deletes a path recursively.
     * <p>
     * If the path is a file or an empty directory, this method is equivalent to
     * {@link Files#delete(Path)}. If the path is a non-empty directory, the
     * directory, all sub-directories, and all child files are deleted.
     * <p>
     * This operation is not atomic with respect to other file system
     * operations. As a result, the path may be left in a partially-deleted
     * state if this method fails.
     *
     * @param path the path to delete
     *
     * @throws NoSuchFileException if the path does not exist
     * @throws IOException if an I/O error occurs while deleting the path
     */
    public static void deleteRecursive(Path path) throws IOException {
        FileSystemProvider provider = path.getFileSystem().provider();
        if (provider instanceof RecursiveDelete) {
            ((RecursiveDelete) provider).deleteRecursive(path);
        } else if (!fileTreeDelete(path)) {
            throw new NoSuchFileException(path.toString());
        }
    }

    /**
     * Deletes a path recursively if it exists.
     * <p>
     * If the path is a file or an empty directory, this method is equivalent to
     * {@link Files#deleteIfExists(Path)}. If the path is a non-empty directory,
     * the directory, all sub-directories, and all child files are deleted.
     * <p>
     * This operation is not atomic with respect to other file system
     * operations. As a result, the path may be left in a partially-deleted
     * state if this method fails.
     *
     * @param path the path to delete
     *
     * @return {@code true} if the path was deleted by this method,
     *         {@code false} if the path did not exist
     *
     * @throws IOException if an I/O error occurs while deleting the path
     */
    public static boolean deleteRecursiveIfExists(Path path) throws IOException {
        FileSystemProvider provider = path.getFileSystem().provider();
        if (provider instanceof RecursiveDelete) {
            return ((RecursiveDelete) provider).deleteRecursiveIfExists(path);
        } else {
            return fileTreeDelete(path);
        }
    }

    private static boolean fileTreeDelete(Path path) throws IOException {
        try {
            Files.walkFileTree(path, new DeleteVisitor());
        } catch (NoSuchFileException e) {
            return false;
        }
        return true;
    }

    private static final class DeleteVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException {
            // visitFile may fail because delete fails or attributes are not
            // available. Try deleting again for the second case.
            Throwables.propagateIfInstanceOf(exc, NoSuchFileException.class);
            try {
                Files.delete(file);
            } catch (IOException ignored) {
                // don't hide the original exception with a failed retry
                throw exc;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
            if (exc != null) {
                throw exc;
            }
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Copy a file to a target file in an efficient way.
     * <p>
     * This method is equivalent to
     * {@link Files#copy(Path, Path, java.nio.file.CopyOption...)
     * Files.copy(source, target, StandardCopyOptions.REPLACE_EXISTING)}, but
     * may be significantly faster depending on the file systems of the source
     * and the target.
     *
     * @param source the file to copy
     * @param target the target file
     *
     * @throws IllegalArgumentException if {@code source} is not a regular file
     *         or {@code target} is a directory.
     * @throws IOException if an IO error occurs while copying
     */
    public static void copyLarge(Path source, Path target) throws IOException {
        checkArgument(Files.isRegularFile(source),
                "source (%s) must be a regular file.", source.toAbsolutePath());
        checkArgument(!Files.isDirectory(target),
                "target (%s) cannot be a directory.", target.toAbsolutePath());

        FileSystemProvider sourceProvider = source.getFileSystem().provider();
        FileSystemProvider targetProvider = target.getFileSystem().provider();

        if (sourceProvider instanceof LargeFileCopy) {
            try {
                ((LargeFileCopy) sourceProvider).copyLarge(source, target);
                return;
            } catch (UnsupportedOperationException e) {
                // Swallow... fall back to other copy strategies
            }
        }

        if (targetProvider instanceof LargeFileCopy) {
            try {
                ((LargeFileCopy) targetProvider).copyLarge(source, target);
                return;
            } catch (UnsupportedOperationException e) {
                // Swallow... fall back to other copy strategies
            }
        }

        // Default
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copy a path to a target path recursively.
     * <p>
     * If the source is a file or empty directory, this method is equivalent to
     * {@link Files#copy(Path, Path, java.nio.file.CopyOption...)
     * Files.copy(source, target)}. If the source is a non-empty directory, the
     * directory and all descendants are copied to the target path. The
     * destination must not exist.
     * <p>
     * If an {@code IOException} occurs while copying files and directories,
     * this method may leave a partial copy at the target path. Users may catch
     * these exceptions and recursively delete the target path if this is a
     * problem.
     *
     * @param source the path to copy
     * @param target the target path
     *
     * @throws IllegalArgumentException if the destination already exists or one
     *         or more parent directories do not exist.
     * @throws IOException if an I/O error occurs while copying
     */
    public static void copyRecursive(Path source, Path target) throws IOException {
        checkArgument(!Files.exists(target),
                "target (%s) already exists.", target.toAbsolutePath());
        checkArgument(Files.isDirectory(target.getParent()),
                "parent directory for target (%s) does not exist.", target.toAbsolutePath());

        FileSystemProvider sourceProvider = source.getFileSystem().provider();
        FileSystemProvider targetProvider = target.getFileSystem().provider();

        if (sourceProvider instanceof RecursiveCopy) {
            try {
                ((RecursiveCopy) sourceProvider).copyRecursive(source, target);
                return;
            } catch (UnsupportedOperationException e) {
                // Swallow... fall back to other copy strategies
            }
        }

        if (targetProvider instanceof RecursiveCopy) {
            try {
                ((RecursiveCopy) targetProvider).copyRecursive(source, target);
                return;
            } catch (UnsupportedOperationException e) {
                // Swallow... fall back to other copy strategies
            }
        }

        Files.walkFileTree(source, new CopyVisitor(source, target));
    }

    private static final class CopyVisitor extends SimpleFileVisitor<Path> {
        private final Map<Path, Set<PosixFilePermission>> perms = new HashMap<>();

        private final Path source;
        private final Path target;

        CopyVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            // store permissions to set in postVisitDirectory in case the
            // source permissions are incompatible with writing files
            perms.put(dir, Files.getPosixFilePermissions(dir));
            try {
                Files.createDirectory(resolve(dir));
            } catch (FileAlreadyExistsException ignored) {
                // ignore
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.copy(file, resolve(file));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
            if (exc != null) {
                throw exc;
            }
            Set<PosixFilePermission> dirPerms = perms.get(dir);
            assert dirPerms != null : "no perms for " + dir;
            Files.setPosixFilePermissions(resolve(dir), dirPerms);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Gets the location {@code path} in {@code target}.
         */
        private Path resolve(Path path) {
            Path relative = source.relativize(path);
            Path targetPath = target.toAbsolutePath();
            for (Path pathComponent : relative) {
                targetPath = targetPath.resolve(pathComponent.getFileName().toString());
            }
            return targetPath;
        }
    }

    /**
     * Reads the contents of a file as a string.
     *
     * @param path the file to read
     * @param cs the charset to use for decoding
     *
     * @return a string containing the full contents of the file
     *
     * @throws IOException if an I/O error occurs while reading a file
     *
     * @see Files#readAllBytes(Path)
     */
    public static String readAllString(Path path, Charset cs) throws IOException {
        return cs.decode(ByteBuffer.wrap(Files.readAllBytes(path))).toString();
    }

    /**
     * Writes a string to a file.
     *
     * @param path the path to the file
     * @param string the string to write
     * @param cs the charset to use for encoding
     * @param options options specifying how the file is opened
     *
     * @return the path
     *
     * @throws IOException if an I/O error occurs while writing the file
     *
     * @see Files#write(Path, Iterable, Charset, OpenOption...)
     */
    public static Path write(Path path, String string, Charset cs, OpenOption... options)
            throws IOException {
        try (Writer w = Files.newBufferedWriter(path, cs, options)) {
            w.write(string);
        }
        return path;
    }

    /**
     * Returns the default directory for the given file system.
     */
    public static Path defaultDirectory(FileSystem fs) {
        return fs.getPath("").toAbsolutePath();
    }

    /**
     * Adds a permission to a file or directory.
     *
     * @param path the path to the file or directory
     * @param permission the permission to add
     *
     * @throws IOException if an I/O error occurs while adding the permission
     */
    public static void addPermission(Path path, PosixFilePermission permission)
            throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
        if (perms.add(permission)) {
            Files.setPosixFilePermissions(path, perms);
        }
    }

    /**
     * Recursively adds a permission to a directory.
     *
     * @param path the path to the directory
     * @param permission the permission to add
     *
     * @throws IOException if an I/O error occurs while adding the permission
     */
    public static void addPermissionRecursive(Path path, PosixFilePermission permission)
            throws IOException {
        changePermissionsRecursive(path, PermissionChange.ADD, Collections.singleton(permission));
    }

    /**
     * Adds the {@code OWNER_EXECUTE} permission to a file or directory.
     *
     * @param path the path to the file or directory
     *
     * @throws IOException if an I/O error occurs while setting the permission
     */
    public static void setExecutable(Path path) throws IOException {
        addPermission(path, PosixFilePermission.OWNER_EXECUTE);
    }

    /**
     * Removes a permission from a file or directory.
     *
     * @param path the path to the file or directory
     * @param permission the permission to remove
     *
     * @throws IOException if an I/O error occurs while removing the permission
     */
    public static void removePermission(Path path, PosixFilePermission permission)
            throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
        if (perms.remove(permission)) {
            Files.setPosixFilePermissions(path, perms);
        }
    }

    /**
     * Recursively removes a permission from a directory.
     *
     * @param path the path to the directory
     * @param permission the permission to remove
     *
     * @throws IOException if an I/O error occurs while removing the permission
     */
    public static void removePermissionRecursive(Path path, PosixFilePermission permission)
            throws IOException {
        changePermissionsRecursive(
                path, PermissionChange.REMOVE, Collections.singleton(permission));
    }

    /**
     * Recursively sets permissions for a directory.
     *
     * @param path the path to the directory
     * @param permissions the desired POSIX file permissions
     *
     * @throws IOException if an I/O error occurs while setting the permission
     */
    public static void setPermissionRecursive(Path path, Set<PosixFilePermission> permissions)
            throws IOException {
        changePermissionsRecursive(path, PermissionChange.SET, permissions);
    }

    private static void changePermissionsRecursive(Path path, PermissionChange change,
            Set<PosixFilePermission> permissions) throws IOException {
        FileSystemProvider provider = path.getFileSystem().provider();
        if (provider instanceof RecursivePermissions) {
            RecursivePermissions recursiveProvider = (RecursivePermissions) provider;
            recursiveProvider.changePermissionsRecursive(path, change, permissions);
        } else {
            Files.walkFileTree(path, new PermissionVisitor(change, permissions));
        }
    }

    private static final class PermissionVisitor extends SimpleFileVisitor<Path> {

        private final PermissionChange change;
        private final Set<PosixFilePermission> permissions;

        PermissionVisitor(PermissionChange change, Set<PosixFilePermission> permissions) {
            this.change = change;
            this.permissions = permissions;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            changePermissions(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            changePermissions(dir);
            return FileVisitResult.CONTINUE;
        }

        private void changePermissions(Path path) throws IOException {
            final Set<PosixFilePermission> perms;
            switch (change) {
                case ADD:
                    perms = Files.getPosixFilePermissions(path);
                    perms.addAll(permissions);
                    break;
                case REMOVE:
                    perms = Files.getPosixFilePermissions(path);
                    perms.removeAll(permissions);
                    break;
                case SET:
                    perms = permissions;
                    break;
                default:
                    throw new IllegalArgumentException("unknown change: " + change);
            }
            Files.setPosixFilePermissions(path, perms);
        }
    }

    /**
     * Test if the given path is empty. Directories are empty if they contain no
     * entries. Files are empty if they exist and their
     * {@linkplain Files#size(Path) size} is zero. Paths that do not exist are
     * always empty.
     *
     * @param path the path
     *
     * @return {@code true} if the path is empty
     *
     * @throws IOException if an I/O error occurs while accessing the path
     */
    public static boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                return !ds.iterator().hasNext();
            }
        } else if (Files.exists(path)) {
            return Files.size(path) == 0;
        } else {
            return true;
        }
    }

    /**
     * Given a {@code Path} representing a directory, retrieve the list of files
     * and directories contained in that {@code Path} as a {@code List}. The
     * {@code Path} objects are obtained as if by resolving the name of the
     * directory objects against the given {@code Path}.
     *
     * @param directoryPath The {@code Path} to get the directory entries for
     *
     * @return A list of {@code Path} objects representing the directory entries
     *
     * @throws IOException If an I/O error occurs while accessing the path
     */
    public static List<Path> listDirectory(Path directoryPath) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
            return Lists.newArrayList(directoryStream);
        }
    }

    private MoreFiles() {
        throw new UnsupportedOperationException();
    }
}
