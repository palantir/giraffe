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
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.palantir.giraffe.file.base.BaseFileSystemProvider;
import com.palantir.giraffe.file.base.CopyFlags;
import com.palantir.giraffe.file.base.CrossSystemTransfers;
import com.palantir.giraffe.file.base.LinkOptions;
import com.palantir.giraffe.file.base.attribute.ChmodFilePermissions;
import com.palantir.giraffe.file.base.attribute.PermissionChange;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViews;
import com.palantir.giraffe.file.base.feature.LargeFileCopy;
import com.palantir.giraffe.file.base.feature.RecursiveCopy;
import com.palantir.giraffe.file.base.feature.RecursiveDelete;
import com.palantir.giraffe.file.base.feature.RecursivePermissions;

import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

/**
 * Abstract {@code FileSystemProvider} implementation for SSH-based file
 * systems.
 *
 * @author bkeyes
 *
 * @param <P> the type of path used by the provider's file systems
 */
public abstract class BaseSshFileSystemProvider<P extends BaseSshPath<P>>
        extends BaseFileSystemProvider<P>
        implements RecursiveDelete, RecursivePermissions, LargeFileCopy, RecursiveCopy {

    protected BaseSshFileSystemProvider(Class<P> pathClass) {
        super(pathClass);
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        checkUri(uri);

        Logger logger = SshEnvironments.getLogger(env);
        SharedSshClient client = SshEnvironments.getClient(env, getConnectionFactory());
        return newFileSystem(new SshSystemContext(uri, client, logger));
    }

    protected abstract SshConnectionFactory getConnectionFactory();

    protected abstract BaseSshFileSystem<P> newFileSystem(SshSystemContext context);

    @Override
    public FileSystem getFileSystem(URI uri) {
        checkUri(uri);
        throw new FileSystemNotFoundException(uri.toString());
    }

    protected abstract void checkUri(URI uri);

    @Override
    public void createSymbolicLink(Path link, Path target,
            FileAttribute<?>... attrs) throws IOException {
        P linkToCreate = checkPath(link);
        P targetFile = checkPath(target);
        if (!isSameUri(linkToCreate, targetFile)) {
            // TODO(bkeyes): fix this exception (IOException)
            throw new InvalidPathException(link.toString(),
                    "has a different URI than target " + target.toString());
        }
        logger(linkToCreate).debug("symlinking {} to {}", link, target);
        SshSymlinks.create(linkToCreate, targetFile, attrs);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        P symlink = checkPath(link);
        return link.getFileSystem().getPath(SshSymlinks.read(symlink));
    }

    @Override
    public void deleteRecursive(Path path) throws IOException {
        if (!deleteRecursiveIfExists(path)) {
            throw new NoSuchFileException(path.toString());
        }
    }

    @Override
    public boolean deleteRecursiveIfExists(Path path) throws IOException {
        P file = checkPath(path);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            logger(file).debug("recursively deleting {}", file);
            SshSameHostFileHelper.deleteRecursive(file);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void changePermissionsRecursive(Path path, PermissionChange change,
            Set<PosixFilePermission> permissions) throws IOException {
        P file = checkPath(path);
        String mode = ChmodFilePermissions.toMode(change, permissions);
        logger(file).debug("recursively changing permissions of {} to {}", file, mode);
        SshSameHostFileHelper.changePermissionsRecursive(file, mode);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        P sourceFile = checkPath(source);
        P targetFile = checkPath(target);

        CopyFlags flags = CopyFlags.fromOptions(options);
        if (!Files.isSameFile(sourceFile, targetFile)) {
            if (Files.isDirectory(sourceFile, LinkOptions.toArray(flags.followLinks))) {
                logger(sourceFile).debug("copying directory {} to {}", source, target.toUri());
                copyDirectory(sourceFile, targetFile, flags);
            } else if (isSameUri(sourceFile, targetFile)) {
                logger(sourceFile).debug("copying file {} to {}", source, target);
                SshSameHostFileHelper.copyFile(sourceFile, targetFile, flags);
            } else {
                logger(sourceFile).debug("copying file {} to {}", source, target.toUri());
                CrossSystemTransfers.copyFile(sourceFile, targetFile, flags);
            }
        }
    }

    @Override
    public void copyLarge(Path source, Path target) throws IOException {
        copyRecursive(source, target);
    }

    @Override
    public void copyRecursive(Path source, Path target) throws IOException {
        boolean isSourceSsh = isCompatible(source);
        boolean isTargetSsh = isCompatible(target);
        String absSource = source.toAbsolutePath().toString();
        String absTarget = target.toAbsolutePath().toString();

        if (isLocal(source) || isLocal(target)) {
            if (isTargetSsh) {
                P sshTarget = checkPath(target);
                logger(sshTarget).debug("scp from {} to {}", source.toUri(), absTarget);
                SCPFileTransfer scp = sshTarget.getFileSystem().getScpFileTransfer();
                scp.upload(absSource, absTarget);
            } else {
                P sshSource = checkPath(source);
                logger(sshSource).debug("scp from {} to {}", absSource, target.toUri());
                SCPFileTransfer scp = sshSource.getFileSystem().getScpFileTransfer();
                scp.download(absSource, absTarget);
            }
        } else if (isSourceSsh && isTargetSsh && isSameUri(checkPath(source), checkPath(target))) {
            P sshSource = checkPath(source);
            P sshTarget = checkPath(target);
            logger(sshSource).debug("recursively copying path {} to {}", absSource, absTarget);
            SshSameHostFileHelper.copyRecursive(sshSource, sshTarget);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void copyDirectory(P source, P target, CopyFlags flags)
            throws IOException {
        createDirectory(target);
        if (flags.copyAttributes) {
            LinkOption[] options = LinkOptions.toArray(flags.followLinks);
            PosixFileAttributeViews.copyAttributes(source, target, options);
        }
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        P sourceFile = checkPath(source);
        P targetFile = checkPath(target);

        CopyFlags flags = CopyFlags.fromOptions(options);
        if (!Files.isSameFile(sourceFile, targetFile)) {
            if (isSameUri(sourceFile, targetFile)) {
                logger(sourceFile).debug("moving path {} to {}", source, target);
                SshSameHostFileHelper.movePath(sourceFile, targetFile, flags);
            } else {
                logger(sourceFile).debug("moving path {} to {}", source, target.toUri());
                moveCrossHost(sourceFile, targetFile, flags);
            }
        }
    }

    private void moveCrossHost(P source, P target, CopyFlags flags)
            throws IOException {
        BasicFileAttributes attrs = readAttributes(source, BasicFileAttributes.class);
        if (attrs.isDirectory()) {
            CrossSystemTransfers.moveDirectory(source, target, flags);
        } else {
            CrossSystemTransfers.moveFile(source, target, flags);
        }
    }

    private boolean isSameUri(P path, P other) {
        return path.getFileSystem().uri().equals(other.getFileSystem().uri());
    }

    @Override
    public Path getPath(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    private static Logger logger(BaseSshPath<?> path) {
        return path.getFileSystem().logger();
    }

    private static boolean isLocal(Path path) {
        return path.getFileSystem().equals(FileSystems.getDefault());
    }
}
