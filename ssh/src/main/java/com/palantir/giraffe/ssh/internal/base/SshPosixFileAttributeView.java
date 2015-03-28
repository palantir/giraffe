package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.palantir.giraffe.file.base.attribute.AbstractPosixFileAttributeView;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeSetter;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;

final class SshPosixFileAttributeView extends AbstractPosixFileAttributeView {

    private final BaseSshPath<?> path;
    private final boolean followLinks;

    SshPosixFileAttributeView(BaseSshPath<?> path, boolean followLinks) {
        this.path = path;
        this.followLinks = followLinks;
    }

    @Override
    public SshPosixFileAttributes readAttributes() throws IOException {
        try (SFTPClient sftp = path.getFileSystem().openSftpClient()) {
            FileAttributes attrs;
            if (followLinks) {
                attrs = sftp.stat(path.toString());
            } else {
                attrs = sftp.lstat(path.toString());
            }
            return new SshPosixFileAttributes(attrs, path.getInode());
        } catch (SFTPException e) {
            // TODO(bkeyes): move this laundering to a utility
            if (e.getStatusCode() == StatusCode.NO_SUCH_FILE) {
                throw new NoSuchFileException(path.toString());
            } else {
                throw e;
            }
        }
    }

    @Override
    protected PosixFileAttributeSetter newSetter() {
        return new SshAttributeSetter(path);
    }

}
