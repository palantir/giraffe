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
import java.nio.file.Path;

import com.palantir.giraffe.host.AuthenticatedHostAccessor;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.ssh.internal.SshHostControlSystem;
import com.palantir.giraffe.ssh.internal.SshUris;

/**
 * Provides access to a file system and execution system on a host using SSH.
 *
 * @author bkeyes
 */
public final class SshHostAccessor implements AuthenticatedHostAccessor<SshCredential> {

    private static final int DEFAULT_PORT = 22;

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given password.
     *
     * @param hostname the name of the host to access
     * @param username the user to authenticate as
     * @param password the user's password
     */
    public static SshHostAccessor forPassword(String hostname, String username, String password) {
        Host host = Host.fromHostname(hostname);
        return forCredential(host, PasswordSshCredential.of(username, password));
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given password.
     *
     * @param hostname the name of the host to access
     * @param username the user to authenticate as
     * @param password the user's password
     */
    public static SshHostAccessor forPassword(String hostname, String username, char[] password) {
        Host host = Host.fromHostname(hostname);
        return forCredential(host, PasswordSshCredential.of(username, password));
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given
     * private key.
     *
     * @param hostname the name of the host to access
     * @param username the user to authenticate as
     * @param keyFile the private key file; the matching public key must be
     *        present on the server
     */
    public static SshHostAccessor forKey(String hostname, String username, Path keyFile)
            throws IOException {
        Host host = Host.fromHostname(hostname);
        return forCredential(host, PublicKeySshCredential.fromFile(username, keyFile));
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given
     * private key.
     *
     * @param hostname the name of the host to access
     * @param username the user to authenticate as
     * @param privateKey the private key; the matching public key must be
     *        present on the server
     */
    public static SshHostAccessor forKey(String hostname, String username, String privateKey) {
        Host host = Host.fromHostname(hostname);
        return forCredential(host, PublicKeySshCredential.of(username, privateKey));
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates as the given
     * user using the system Kerberos configuration.
     *
     * @param hostname the name of the host to access
     * @param username the user to authenticate as
     */
    public static SshHostAccessor forKerberos(String hostname, String username) {
        Host host = Host.fromHostname(hostname);
        return forCredential(host, KerberosSshCredential.of(username));
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given
     * {@link SshCredential}.
     *
     * @param host the host to access
     * @param credential the credential to use for authentication
     */
    public static SshHostAccessor forCredential(Host host, SshCredential credential) {
        return forCredential(host, DEFAULT_PORT, credential);
    }

    /**
     * Returns a new {@code SshHostAccessor} that authenticates using the given
     * {@link SshCredential} on the given port.
     *
     * @param host the host to access
     * @param port the port that SSH is listening on
     * @param credential the credential to use for authentication
     */
    public static SshHostAccessor forCredential(Host host, int port, SshCredential credential) {
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
        return SshHostControlSystem.builder(request)
            .setFileSystem()
            .setExecutionSystem()
            .build();
    }
}
