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
