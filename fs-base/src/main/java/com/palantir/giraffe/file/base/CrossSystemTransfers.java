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
package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViews;

/**
 * Utilities to implement copy and move operations between distinct systems from
 * the same provider. For example, some providers create file systems on
 * different remote hosts.
 *
 * @author bkeyes
 */
public final class CrossSystemTransfers {

    public static void copyFile(Path source, Path target, CopyFlags flags) throws IOException {
        checkPaths(source, target, flags);

        try (ReadableByteChannel sourceChannel = openSourceChannel(source);
             WritableByteChannel targetChannel = openTargetChannel(target, flags)) {
            ByteStreams.copy(sourceChannel, targetChannel);
        }

        if (flags.copyAttributes) {
            LinkOption[] options = LinkOptions.toArray(flags.followLinks);
            PosixFileAttributeViews.copyAttributes(source, target, options);
        }
    }

    private static ReadableByteChannel openSourceChannel(Path source) throws IOException {
        return Files.newByteChannel(source, StandardOpenOption.READ);
    }

    private static WritableByteChannel openTargetChannel(Path target, CopyFlags flags)
            throws IOException {
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.WRITE);
        if (flags.replaceExisting) {
            options.add(StandardOpenOption.CREATE);
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            options.add(StandardOpenOption.CREATE_NEW);
        }
        return Files.newByteChannel(target, options);
    }

    public static void moveDirectory(Path source, Path target, CopyFlags flags)
            throws IOException {
        checkPaths(source, target, flags);
        checkNoAtomicMove(source, target, flags);

        if (!Iterables.isEmpty(Files.newDirectoryStream(source))) {
            throw new DirectoryNotEmptyException(source.toString());
        }

        if (flags.replaceExisting) {
            Files.deleteIfExists(target);
        }
        Files.createDirectory(target);
        Files.delete(source);
    }

    public static void moveFile(Path source, Path target, CopyFlags flags) throws IOException {
        checkPaths(source, target, flags);
        checkNoAtomicMove(source, target, flags);

        CopyFlags copyFlags = new CopyFlags();
        copyFlags.replaceExisting = flags.replaceExisting;
        copyFile(source, target, copyFlags);
        Files.delete(source);
    }

    private static void checkPaths(Path source, Path target, CopyFlags flags) {
        checkNotNull(source, "source must be non-null");
        checkNotNull(target, "target must be non-null");
        checkNotNull(flags, "flags must be non-null");
    }

    private static void checkNoAtomicMove(Path source, Path target, CopyFlags flags)
            throws AtomicMoveNotSupportedException {
        if (flags.atomicMove) {
            throw new AtomicMoveNotSupportedException(
                    source.toString(), target.toString(),
                    "paths exist on different systems");
        }
    }

    private CrossSystemTransfers() {
        throw new UnsupportedOperationException();
    }
}
