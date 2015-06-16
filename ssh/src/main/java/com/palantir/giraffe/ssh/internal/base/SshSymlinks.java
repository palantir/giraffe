package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViews;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;

final class SshSymlinks {

    public static void create(BaseSshPath<?> link, BaseSshPath<?> target, FileAttribute<?>[] attrs)
            throws IOException {
        try (SFTPClient sftp = link.getFileSystem().openSftpClient()) {
            // This is a workaround because SshJ has the arguments implemented backwards
            // from what their documentation claims...
            sftp.symlink(target.toString(), link.toString());
            Set<PosixFilePermission> perms = PosixFileAttributeViews.getCreatePermissions(attrs);
            if (perms != null) {
                FileAttributes.Builder builder = new FileAttributes.Builder();
                FileAttributes sftpAttrs = builder.withPermissions(
                        ChmodFilePermissions.toBits(perms)).build();
                sftp.setattr(link.toString(), sftpAttrs);
            }
        }
    }

    public static String read(BaseSshPath<?> symlink) throws IOException {
        try (SFTPClient sftp = symlink.getFileSystem().openSftpClient()) {
            return sftp.readlink(symlink.toString());
        }
    }

    private SshSymlinks() {
        throw new UnsupportedOperationException();
    }
}
