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
import java.nio.file.Path;

import javax.annotation.CheckForNull;

/**
 * Authenticates SSH connections using a public-private key pair.
 *
 * @author bkeyes
 */
public final class PublicKeyCredential extends SshCredential {

    @CheckForNull
    private final Path keyPath;
    private final byte[] privateKey;

    public PublicKeyCredential(String username, byte[] privateKey) {
        this(username, privateKey, null);
    }

    public PublicKeyCredential(String hostUsername, byte[] privateKey, Path keyPath) {
        super(hostUsername);
        this.privateKey = privateKey.clone();
        this.keyPath = keyPath;
    }

    @Override
    public void authenticate(SshAuthenticator authenticator) throws IOException {
        authenticator.authByPublicKey(this, privateKey.clone());
    }

    public byte[] getPrivateKey() {
        return privateKey.clone();
    }

    /**
     * Returns the path of the private key used by this credential or
     * {@code null} if it is not available. For example, a path may be
     * unavailable if this credential was constructed using an in-memory key.
     */
    @CheckForNull
    public Path getKeyPath() {
        return keyPath;
    }
}
