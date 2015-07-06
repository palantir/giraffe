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
package com.palantir.giraffe.ssh;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.palantir.giraffe.SystemConverter;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.file.base.SuppressedCloseable;
import com.palantir.giraffe.host.AbstractHostControlSystem;
import com.palantir.giraffe.host.AuthenticatedHostAccessor;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.ssh.internal.SshUris;

/**
 * Provides access to a file system and execution system on a host using SSH.
 *
 * @author bkeyes
 */
public final class SshHostAccessor implements AuthenticatedHostAccessor<SshCredential> {

    private static final int DEFAULT_PORT = 22;

    public static SshHostAccessor authWithPassword(Host host, String username, String password) {
        return authWithCredential(host, new PasswordCredential(username, password.toCharArray()));
    }

    public static SshHostAccessor authWithPassword(Host host, String username, char[] password) {
        return authWithCredential(host, new PasswordCredential(username, password));
    }

    public static SshHostAccessor authWithPassword(Host host, String username, int port,
            char[] password) {
        return authWithCredential(host, port, new PasswordCredential(username, password));
    }

    public static SshHostAccessor authWithKey(Host host, String username, Path privateKeyPath)
            throws IOException {
        return authWithCredential(host, readKey(username, privateKeyPath));
    }

    public static SshHostAccessor authWithKey(Host host, String username, int port,
            Path privateKeyPath) throws IOException {
        return authWithCredential(host, port, readKey(username, privateKeyPath));
    }

    private static PublicKeyCredential readKey(String username, Path file) throws IOException {
        return new PublicKeyCredential(username, Files.readAllBytes(file), file);
    }

    public static SshHostAccessor authWithKey(Host host, String username, byte[] privateKey) {
        return authWithCredential(host, new PublicKeyCredential(username, privateKey));
    }

    public static SshHostAccessor authWithKey(Host host, String username, int port,
            byte[] privateKey) {
        return authWithCredential(host, port, new PublicKeyCredential(username, privateKey));
    }

    public static SshHostAccessor authWithCredential(Host host, SshCredential credential) {
        return authWithCredential(host, DEFAULT_PORT, credential);
    }

    public static SshHostAccessor authWithCredential(Host host, int port,
            SshCredential credential) {
        URI uri = SshUris.getHostUri(host, port, credential);
        return new SshHostAccessor(new SshSystemRequest(uri, credential));
    }

    private final SshSystemRequest request;

    private SshHostAccessor(SshSystemRequest request) {
        this.request = request;
    }

    @Override
    public Host getHost() {
        return Host.fromUri(request.uri());
    }

    @Override
    public SshSystemRequest request() {
        return request;
    }

    @Override
    public HostControlSystem open() throws IOException {
        FileSystem fs = FileSystems.newFileSystem(
                request.fileSystemUri(),
                request.options(),
                getClass().getClassLoader());

        // use the file system as the canonical system source
        return new SshHostControlSystem(request.uri(), fs, SystemConverter.asExecutionSystem(fs));
    }

    private static final class SshHostControlSystem extends AbstractHostControlSystem {
        private SshHostControlSystem(URI uri, FileSystem fs, ExecutionSystem es) {
            super(uri, fs, es);
        }

        @Override
        public void close() throws IOException {
            SuppressedCloseable.create(getExecutionSystem(), getFileSystem()).close();
        }
    }
}
