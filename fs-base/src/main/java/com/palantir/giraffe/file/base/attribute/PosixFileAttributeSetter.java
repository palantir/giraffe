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

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/**
 * An alternative to {@link java.nio.file.attribute.PosixFileAttributeView
 * PosixFileAttributeView} that allows bulk editing of POSIX file attributes.
 */
public interface PosixFileAttributeSetter {

    PosixFileAttributeSetter lastModifiedTime(FileTime time);

    PosixFileAttributeSetter lastAccessTime(FileTime time);

    PosixFileAttributeSetter creationTime(FileTime time);

    PosixFileAttributeSetter permissions(Set<PosixFilePermission> perms);

    PosixFileAttributeSetter owner(UserPrincipal owner);

    PosixFileAttributeSetter group(GroupPrincipal group);

    void set() throws IOException;

}
