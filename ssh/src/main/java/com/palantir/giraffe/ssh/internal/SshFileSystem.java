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

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.file.base.BaseFileSystem;
import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.FileAttributeViewRegistry;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViews;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystemUpgradeable;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

final class SshFileSystem extends BaseFileSystem<SshPath> implements HostControlSystemUpgradeable {

    public static final String SEPARATOR = "/";

    private final SshFileSystemProvider provider;
    private final URI uri;
    private final SSHClient client;
    private final Logger logger;
    private final FileAttributeViewRegistry viewRegistry;
    private final CloseContext closeContext;

    private volatile SshPath defaultDirectory;

    private SshHostControlSystem sourceSystem;

    SshFileSystem(SshFileSystemProvider provider, InternalSshSystemRequest request) {
        this.provider = provider;

        this.uri = request.fileSystemUri();
        this.client = request.getClient();
        this.logger = HostLogger.create(request.getLogger(), Host.fromUri(uri));

        SshFileAttributeViewFactory factory = new SshFileAttributeViewFactory(provider);
        viewRegistry = FileAttributeViewRegistry.builder()
                .add(factory.getBasicFactory())
                .add(factory.getPosixFactory())
                .build();

        closeContext = request.getCloseContext();
    }

    @Override
    public SshFileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        closeContext.close();
    }

    @Override
    public boolean isOpen() {
        return !closeContext.isClosed();
    }

    @Override
    public SshPath defaultDirectory() {
        if (defaultDirectory == null) {
            try (SFTPClient sftp = openSftpClient()) {
                defaultDirectory = getPath(sftp.canonicalize(""));
            } catch (IOException e) {
                throw new IOError(e);
            }
        }
        return defaultDirectory;
    }

    void setSourceSystem(SshHostControlSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @Override
    public HostControlSystem asHostControlSystem() throws IOException {
        checkOpen();

        assert sourceSystem != null : "source HostControlSystem was never set";
        return sourceSystem.asView();
    }

    CommandResult execute(String executable, Object... args) throws IOException {
        return execute(executable, Arrays.asList(args));
    }

    CommandResult execute(String executable, List<Object> args) throws IOException {
        ExecutionSystem es = sourceSystem.getExecutionSystem();
        Command cmd = es.getCommandBuilder(executable).addArguments(args).build();
        return Commands.execute(cmd, CommandContext.ignoreExitStatus());
    }

    SFTPClient openSftpClient() throws IOException {
        checkOpen();
        return client.newSFTPClient();
    }

    SCPFileTransfer getScpFileTransfer() {
        checkOpen();
        return client.newSCPFileTransfer();
    }

    Logger logger() {
        return logger;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public SshPath getPath(String path) {
        return new SshPath(this, SshPath.PARSER.parse(path));
    }

    @Override
    public FileAttributeViewRegistry fileAttributeViews() {
        return viewRegistry;
    }

    @Override
    protected void delete(SshPath path) throws IOException {
        String pathString = path.toString();
        logger.debug("deleting {}", pathString);

        boolean isDirectory = false;
        try (SFTPClient sftp = openSftpClient()) {
            isDirectory = sftp.lstat(pathString).getType() == Type.DIRECTORY;
            if (isDirectory) {
                sftp.rmdir(pathString);
            } else {
                sftp.rm(pathString);
            }
        } catch (SFTPException e) {
            if (isDirectory && e.getStatusCode() == StatusCode.FAILURE) {
                // TODO(bkeyes): Can this result from other cases?
                throw new DirectoryNotEmptyException(path.toString());
            } else if (e.getStatusCode() == StatusCode.NO_SUCH_FILE) {
                throw new NoSuchFileException(pathString);
            } else {
                throw e;
            }
        }
    }

    @Override
    protected boolean isHidden(SshPath path) throws IOException {
        return path.getFileName().toString().startsWith(".");
    }

    @Override
    protected void checkAccess(SshPath path, AccessMode... modes) throws IOException {
        Set<String> tests = new HashSet<>(4);
        for (AccessMode mode : modes) {
            switch (mode) {
                case READ:
                    tests.add("-r");
                    break;
                case WRITE:
                    tests.add("-w");
                    break;
                case EXECUTE:
                    tests.add("-x");
                    break;
            }
        }

        if (tests.isEmpty()) {
            tests.add("-e");
        }

        List<Object> args = new ArrayList<>();
        Iterator<String> testIter = tests.iterator();
        while (testIter.hasNext()) {
            args.add(testIter.next());
            args.add(path);
            if (testIter.hasNext()) {
                args.add("-a");
            }
        }

        CommandResult result = execute("test", args);
        if (result.getExitStatus() != 0) {
            if (tests.contains("-e")) {
                throw new NoSuchFileException(path.toString());
            } else {
                throw new AccessDeniedException(path.toString());
            }
        }
    }

    @Override
    protected boolean isSameFile(SshPath path, SshPath other) throws IOException {
        if (!uri.equals(other.getFileSystem().uri())) {
            return false;
        } else {
            String inode = path.getInode();
            String inode2 = other.getInode();
            if (inode == null || inode2 == null) {
                return false;
            } else {
                return inode.equals(inode2);
            }
        }
    }

    @Override
    protected void createDirectory(SshPath path, FileAttribute<?>... attrs) throws IOException {
        String pathString = path.toString();
        logger.debug("creating directory {}", pathString);

        try (SFTPClient sftp = openSftpClient()) {
            Set<PosixFilePermission> perms = PosixFileAttributeViews.getCreatePermissions(attrs);
            if (perms == null) {
                perms = PosixFilePermissions.fromString("rwxr-xr-x");
            }

            sftp.mkdir(pathString);
            sftp.setattr(pathString, new FileAttributes.Builder()
                    .withPermissions(ChmodFilePermissions.toBits(perms)).build());
        } catch (SFTPException e) {
            // TODO(jchien): Can this result from other cases?
            if (e.getStatusCode() == StatusCode.FAILURE) {
                throw new FileAlreadyExistsException(pathString);
            } else if (e.getStatusCode() == StatusCode.PERMISSION_DENIED) {
                throw new AccessDeniedException(pathString);
            } else {
                throw e;
            }
        }
    }

    @Override
    protected SeekableByteChannel newByteChannel(SshPath path, Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        logger.debug("opening byte channel for {} with options {}", path, options);
        return registerCloseable(SshSeekableByteChannel.open(path, options, attrs));
    }

    @Override
    protected DirectoryStream<Path> newDirectoryStream(SshPath dir, Filter<? super Path> filter)
            throws IOException {
        logger.debug("opening directory stream for {}", dir);
        try (SFTPClient sftp = openSftpClient()) {
            List<RemoteResourceInfo> entries = sftp.ls(dir.toString());
            return registerCloseable(new SshDirectoryStream(dir, entries, filter));
        }
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.<Path>singletonList(getPath(SEPARATOR));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    <C extends Closeable> C registerCloseable(C closeable) {
        closeContext.registerCloseable(closeable);
        return closeable;
    }

    void unregisterCloseable(Closeable closeable) {
        closeContext.unregister(closeable);
    }

    private void checkOpen() {
        if (!isOpen()) {
            throw new ClosedFileSystemException();
        }
    }
}
