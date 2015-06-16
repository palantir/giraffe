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
package com.palantir.giraffe.file.base.attribute;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

import javax.annotation.CheckForNull;

/**
 * Utility methods for modifying POSIX file attributes.
 *
 * @author bkeyes
 */
// TODO(bkeyes): better name?
public final class PosixFileAttributeViews {

    /**
     * Gets a set of file permissions used when creating a file or directory
     * from the specified attributes. If multiple permission attributes exist,
     * the last value is used.
     *
     * @param attrs the array of attributes to convert
     *
     * @throws UnsupportedOperationException if the array contains
     *         non-permission attributes
     */
    @CheckForNull
    public static Set<PosixFilePermission> getCreatePermissions(FileAttribute<?>[] attrs) {
        FileAttribute<?> permissions = null;
        for (FileAttribute<?> attr : attrs) {
            String name = attr.name();
            if (name.equals("posix:permissions") || name.equals("unix:permissions")) {
                permissions = attr;
            } else {
                throw new UnsupportedOperationException("'" + name + "' cannot be set initally");
            }
        }

        if (permissions != null) {
            // attributes named "posix:permissions" are defined to have type
            // Set<PosixFilePermission>, so if this cast should be safe
            @SuppressWarnings("unchecked")
            Set<PosixFilePermission> set = (Set<PosixFilePermission>) permissions.value();
            return set;
        } else {
            return null;
        }
    }

    public static PosixFileAttributeSetter addAttributes(PosixFileAttributeSetter setter,
            PosixFileAttributes attrs) {
        return setter.lastModifiedTime(attrs.lastModifiedTime())
                .lastAccessTime(attrs.lastAccessTime())
                .creationTime(attrs.creationTime())
                .permissions(attrs.permissions())
                .owner(attrs.owner())
                .group(attrs.group());
    }

    public static PosixFileAttributeSetter addAttributes(PosixFileAttributeSetter setter,
            FileAttribute<?>[] attrs) {
        for (FileAttribute<?> attr : attrs) {
            addAttribute(setter, checkNotNull(attr, "attribute must be non-null"));
        }
        return setter;
    }

    private static void addAttribute(PosixFileAttributeSetter setter, FileAttribute<?> attr) {
        String name = attr.name();
        switch (name) {
            case "basic:lastModifiedTime":
            case "posix:lastModifiedTime":
                setter.lastModifiedTime((FileTime) attr.value());
                break;
            case "basic:lastAccessTime":
            case "posix:lastAccessTime":
                setter.lastAccessTime((FileTime) attr.value());
                break;
            case "basic:creationTime":
            case "posix:creationTime":
                setter.lastAccessTime((FileTime) attr.value());
                break;
            case "posix:permissions":
                @SuppressWarnings("unchecked")
                Set<PosixFilePermission> value = (Set<PosixFilePermission>) attr.value();
                setter.permissions(value);
                break;
            case "posix:owner":
                setter.owner((UserPrincipal) attr.value());
                break;
            case "posix:group":
                setter.group((GroupPrincipal) attr.value());
                break;
            default:
                throw new UnsupportedOperationException("unsupported attribute '" + name + "'");
        }
    }

    public static void copyAttributes(Path source, Path target, LinkOption... options)
            throws IOException {
        PosixFileAttributes attrs = Files.readAttributes(source,
                PosixFileAttributes.class,
                options);

        PosixFileAttributeView view = Files.getFileAttributeView(target,
                PosixFileAttributeView.class,
                options);

        // TODO(bkeyes): any good way to do this in bulk?
        view.setTimes(attrs.lastModifiedTime(), attrs.lastAccessTime(), attrs.creationTime());
        view.setPermissions(attrs.permissions());
        view.setOwner(attrs.owner());
        view.setGroup(attrs.group());
    }

    private PosixFileAttributeViews() {
        throw new UnsupportedOperationException();
    }
}
