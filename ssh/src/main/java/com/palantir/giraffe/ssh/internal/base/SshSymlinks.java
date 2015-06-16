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
