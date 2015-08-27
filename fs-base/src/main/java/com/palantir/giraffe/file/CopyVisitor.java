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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class CopyVisitor extends SimpleFileVisitor<Path> {

    private interface SavedPermissions {
        void set(Path path) throws IOException;
    }

    private final Map<Path, SavedPermissions> directoryPerms = new HashMap<>();

    private final Path source;
    private final Path target;

    CopyVisitor(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        // set permissions on post-visit because if we create read-only
        // directories now, we can't copy files into them
        directoryPerms.put(dir, readPermissions(dir));
        Files.createDirectory(resolve(dir));
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

        SavedPermissions savedPerms = directoryPerms.get(dir);
        assert savedPerms != null : "no saved permissions for " + dir;
        savedPerms.set(resolve(dir));

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

    private static SavedPermissions readPermissions(Path path) throws IOException {
        Set<String> supportedViews = path.getFileSystem().supportedFileAttributeViews();
        if (supportedViews.contains("posix")) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            return new PosixSavedPermissions(perms);
        } else if (supportedViews.contains("dos")) {
            DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class);
            return new DosSavedPermissions(attrs);
        } else {
            return new NoSavedPermissions();
        }
    }

    private static final class NoSavedPermissions implements SavedPermissions {
        @Override
        public void set(Path path) throws IOException {}
    };

    private static final class PosixSavedPermissions implements SavedPermissions {
        private final Set<PosixFilePermission> perms;

        PosixSavedPermissions(Set<PosixFilePermission> perms) {
            this.perms = perms;
        }

        @Override
        public void set(Path path) throws IOException {
            Files.setPosixFilePermissions(path, perms);
        }
    }

    private static final class DosSavedPermissions implements SavedPermissions {
        private final boolean isReadOnly;

        DosSavedPermissions(DosFileAttributes attrs) {
            isReadOnly = attrs.isReadOnly();
        }

        @Override
        public void set(Path path) throws IOException {
            Files.setAttribute(path, "dos:readonly", isReadOnly);
        }
    }
}
