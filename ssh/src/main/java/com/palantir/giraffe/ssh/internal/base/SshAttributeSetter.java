package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.GroupIdPrincipal;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeSetter;
import com.palantir.giraffe.file.base.attribute.UserIdPrincipal;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;

final class SshAttributeSetter implements PosixFileAttributeSetter {

    private final BaseSshPath<?> path;
    private final FileAttributes.Builder attrs = new FileAttributes.Builder();
    private FileTime atime = null;
    private FileTime mtime = null;
    private UserIdPrincipal owner = null;
    private GroupIdPrincipal group = null;

    public SshAttributeSetter(BaseSshPath<?> path) {
        this.path = path;
    }

    @Override
    public SshAttributeSetter lastModifiedTime(FileTime time) {
        this.mtime = time;
        return this;
    }

    @Override
    public SshAttributeSetter lastAccessTime(FileTime time) {
        this.atime = time;
        return this;
    }

    @Override
    public PosixFileAttributeSetter creationTime(FileTime time) {
        return this;
    }

    @Override
    public SshAttributeSetter permissions(Set<PosixFilePermission> perms) {
        attrs.withPermissions(ChmodFilePermissions.toBits(perms));
        return this;
    }

    @Override
    public SshAttributeSetter owner(UserPrincipal fileOwner) {
        this.owner = UserIdPrincipal.fromUserPrinciple(fileOwner);
        return this;
    }

    @Override
    public SshAttributeSetter group(GroupPrincipal fileGroup) {
        this.group = GroupIdPrincipal.fromGroupPrinciple(fileGroup);
        return this;
    }

    @Override
    public void set() throws IOException {
        try (SFTPClient sftp = path.getFileSystem().openSftpClient()) {
            // TODO(bkeyes): how to respect symlinks on setstat?
            sftp.setattr(path.toString(), getSftpAttributes());
        }
    }

    public FileAttributes getSftpAttributes() {
        if (owner != null || group != null) {
            int uid = owner == null ? 0 : owner.getUid();
            int gid = group == null ? 0 : group.getGid();
            attrs.withUIDGID(uid, gid);
        }
        if (atime != null || mtime != null) {
            long atimeMils = atime == null ? 0L : atime.to(TimeUnit.SECONDS);
            long mtimeMils = mtime == null ? 0L : mtime.to(TimeUnit.SECONDS);
            attrs.withAtimeMtime(atimeMils, mtimeMils);
        }
        return attrs.build();
    }
}
