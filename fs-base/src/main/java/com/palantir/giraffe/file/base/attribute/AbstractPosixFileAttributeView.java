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
 * An abstract implementation of {@link AbstractPosixFileAttributeView} that
 * delegates setter methods to a {@link PosixFileAttributeSetter}.
 *
 * @author bkeyes
 */
public abstract class AbstractPosixFileAttributeView implements AnnotatedPosixFileAttributeView {

    @Override
    public final String name() {
        return FileAttributeViews.POSIX_NAME;
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        return readAttributes().owner();
    }

    @Override
    public void setLastModifiedTime(FileTime time) throws IOException {
        setTimes(time, null, null);
    }

    @Override
    public void setLastAccessTime(FileTime time) throws IOException {
        setTimes(null, time, null);
    }

    @Override
    public void setCreationTime(FileTime time) throws IOException {
        setTimes(null, null, time);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime)
            throws IOException {
        PosixFileAttributeSetter setter = newSetter();
        if (lastModifiedTime != null) {
            setter.lastModifiedTime(lastModifiedTime);
        }
        if (lastAccessTime != null) {
            setter.lastAccessTime(lastAccessTime);
        }
        if (createTime != null) {
            setter.creationTime(createTime);
        }
        setter.set();
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
        newSetter().permissions(perms).set();
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        newSetter().owner(owner).set();
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
        newSetter().group(group).set();
    }

    protected abstract PosixFileAttributeSetter newSetter();

}
