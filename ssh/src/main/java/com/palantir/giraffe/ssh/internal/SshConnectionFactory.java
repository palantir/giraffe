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

import com.palantir.giraffe.ssh.SshAuthenticator;
import com.palantir.giraffe.ssh.SshCredential;
import com.palantir.giraffe.ssh.SshSystemRequest;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

final class SshConnectionFactory {

    private final Config config;

    public SshConnectionFactory(Config sshjConfiguration) {
        this.config = sshjConfiguration;
    }

    public SSHClient newAuthedConnection(SshSystemRequest request) throws IOException {
        SSHClient sshClient = new SSHClient(config);
        sshClient.getTransport().addHostKeyVerifier(new PromiscuousVerifier());

        try {
            sshClient.connect(request.uri().getHost(), request.getPort());
            request.getCredential().authenticate(new Authenticator(sshClient));
        } catch (IOException e) {
            IOUtils.closeQuietly(sshClient);
            throw e;
        }

        return sshClient;
    }

    private final class Authenticator implements SshAuthenticator {
        private final SSHClient client;

        public Authenticator(SSHClient client) {
            this.client = client;
        }

        @Override
        public void authByPassword(SshCredential credential, char[] password)
                throws IOException {
            client.authPassword(credential.getUsername(), password);
        }

        @Override
        public void authByPublicKey(SshCredential credential, byte[] privateKey)
                throws IOException {
            KeyProvider keyProvider = client.loadKeys(new String(privateKey), null, null);
            client.authPublickey(credential.getUsername(), keyProvider);
        }
    }
}
