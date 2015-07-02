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
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import com.palantir.giraffe.file.base.AbstractSeekableByteChannel;
import com.palantir.giraffe.file.base.OpenFlags;
import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViews;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;

final class SshSeekableByteChannel extends AbstractSeekableByteChannel {

    public static SshSeekableByteChannel open(SshPath path, Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        OpenFlags flags = OpenFlags.validateFromOptions(options);
        SFTPClient sftp = path.getFileSystem().openSftpClient();
        try {
            RemoteFile file = open(sftp, path, flags, getAttributes(flags, attrs));
            return new SshSeekableByteChannel(file, path, flags, sftp);
        } catch (IOException e) {
            sftp.close();
            throw e;
        }
    }

    private static FileAttributes getAttributes(OpenFlags flags, FileAttribute<?>[] attrs) {
        if (flags.create || flags.createNew) {
            Set<PosixFilePermission> perms = PosixFileAttributeViews.getCreatePermissions(attrs);
            if (perms != null) {
                return new FileAttributes.Builder()
                        .withPermissions(ChmodFilePermissions.toBits(perms)).build();
            }
        }
        return FileAttributes.EMPTY;
    }

    private static RemoteFile open(SFTPClient sftp, SshPath path, OpenFlags flags,
            FileAttributes attrs) throws IOException {
        String pathString = path.toString();
        boolean exists = sftp.statExistence(pathString) != null;

        if (!exists && !(flags.create || flags.createNew)) {
            throw new NoSuchFileException(pathString);
        }
        if (flags.createNew && exists) {
            throw new FileAlreadyExistsException(pathString);
        }

        EnumSet<OpenMode> openOptions = EnumSet.noneOf(OpenMode.class);
        if (flags.read) {
            openOptions.add(OpenMode.READ);
        }
        if (flags.write) {
            openOptions.add(OpenMode.WRITE);
        }
        if (flags.create || flags.createNew) {
            openOptions.add(OpenMode.CREAT);
        }
        if (flags.append) {
            openOptions.add(OpenMode.APPEND);
        }
        if (flags.truncateExisting) {
            if (flags.create || flags.createNew) {
                openOptions.add(OpenMode.TRUNC);
            } else if (exists) {
                sftp.truncate(pathString, 0);
            }
        }

        return sftp.open(pathString, openOptions, attrs);
    }

    private final RemoteFile file;
    private final SshPath path;
    private final OpenFlags flags;
    private final SFTPClient sftp;

    private SshSeekableByteChannel(RemoteFile file,
                                   SshPath path,
                                   OpenFlags flags,
                                   SFTPClient sftp) {
        this.file = file;
        this.path = path;
        this.flags = flags;
        this.sftp = sftp;
    }

    @Override
    protected void doClose() throws IOException {
        path.getFileSystem().unregisterCloseable(this);
        try {
            path.getFileSystem().logger().debug("closing byte channel for {}", path);
            file.close();
            if (flags.deleteOnClose) {
                // TODO(bkeyes): handle the case where close is never called
                sftp.rm(path.toString());
            }
        } finally {
            sftp.close();
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        checkIsOpen();
        if (dst.remaining() > 0) {
            //  Workaround for Ganymed v2.5 capping num bytes read at one time to 32678.
            final int bytesToRead = Math.min(dst.remaining(), 32768);
            byte[] buffer = new byte[bytesToRead];
            int bytesRead = file.read(position, buffer, 0, bytesToRead);
            if (bytesRead > 0) {
                dst.put(buffer, 0, bytesRead);
                position += bytesRead;
            }
            return bytesRead;
        } else {
            return 0;
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        checkIsOpen();

        //  Workaround for SshJ bug. See https://github.com/shikhar/sshj/issues/145.
        final int bytesToRead = Math.min(src.remaining(), 32768);

        byte[] buffer = new byte[bytesToRead];
        src.get(buffer);

        if (flags.append) {
            position = size();
        }

        file.write(position, buffer, 0, buffer.length);
        return advancePosition(buffer.length);
    }

    @Override
    public long size() throws IOException {
        checkIsOpen();
        return file.fetchAttributes().getSize();
    }

    @Override
    public SshSeekableByteChannel truncate(long size) throws IOException {
        checkIsOpen();
        if (truncatePosition(size) && size < size()) {
            sftp.truncate(path.toString(), size);
        }
        return this;
    }
}
