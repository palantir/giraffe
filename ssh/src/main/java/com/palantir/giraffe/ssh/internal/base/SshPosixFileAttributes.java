package com.palantir.giraffe.ssh.internal.base;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.palantir.giraffe.file.base.attribute.AnnotatedPosixFileAttributes;
import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.GroupIdPrincipal;
import com.palantir.giraffe.file.base.attribute.UserIdPrincipal;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.xfer.FilePermission;

final class SshPosixFileAttributes implements AnnotatedPosixFileAttributes {

    private final FileAttributes attrs;
    private final String inode;

    SshPosixFileAttributes(FileAttributes attrs, String inode) {
        this.attrs = attrs;
        this.inode = inode;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(attrs.getMtime(), TimeUnit.SECONDS);
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(attrs.getAtime(), TimeUnit.SECONDS);
    }

    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return attrs.getMode().getType() == Type.REGULAR;
    }

    @Override
    public boolean isDirectory() {
        return attrs.getMode().getType() == Type.DIRECTORY;
    }

    @Override
    public boolean isSymbolicLink() {
        return attrs.getMode().getType() == Type.SYMKLINK;
    }

    @Override
    public boolean isOther() {
        return !isRegularFile() && !isDirectory() && !isSymbolicLink();
    }

    @Override
    public long size() {
        if (attrs.getSize() == 0L) {
            return -1;
        } else {
            return attrs.getSize();
        }
    }

    @Override
    public Object fileKey() {
        return inode;
    }

    @Override
    public UserPrincipal owner() {
        if (attrs.getUID() == 0) {
            return new UserIdPrincipal(-1);
        } else {
            return new UserIdPrincipal(attrs.getUID());
        }
    }

    @Override
    public GroupPrincipal group() {
        if (attrs.getGID() == 0) {
            return new GroupIdPrincipal(-1);
        } else {
            return new GroupIdPrincipal(attrs.getGID());
        }
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return ChmodFilePermissions.toPermissions(FilePermission.toMask(attrs.getPermissions()));
    }
}
