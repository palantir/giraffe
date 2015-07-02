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
package com.palantir.giraffe.ssh.internal;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.palantir.giraffe.file.base.attribute.AbstractPosixFileAttributeView;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeSetter;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;

final class SshPosixFileAttributeView extends AbstractPosixFileAttributeView {

    private final SshPath path;
    private final boolean followLinks;

    SshPosixFileAttributeView(SshPath path, boolean followLinks) {
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
