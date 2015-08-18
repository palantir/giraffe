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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Optional;

/**
 * Authenticates SSH connections using the <em>private key</em> of a key pair.
 * The target server must already have the associated public key.
 * <p>
 * This class optionally provides a path to the private key on the file system,
 * which is useful to interoperate with external commands like {@code rsync}
 * which may not take keys directly. For this reason, clients are encourages to
 * use the {@link #fromFile(String, Path) fromFile} factory method when the
 * private key is stored in a file.
 *
 * @author bkeyes
 */
public final class PublicKeySshCredential extends SshCredential {

    public static PublicKeySshCredential of(String username, String privateKey) {
        byte[] privateKeyBytes = privateKey.getBytes(StandardCharsets.US_ASCII);
        return new PublicKeySshCredential(username, privateKeyBytes, Optional.<Path>absent());
    }

    public static PublicKeySshCredential of(String username, byte[] privateKey) {
        return new PublicKeySshCredential(username, privateKey, Optional.<Path>absent());
    }

    public static PublicKeySshCredential fromFile(String username, Path privateKeyFile)
            throws IOException {
        byte[] privateKey = Files.readAllBytes(privateKeyFile);
        return new PublicKeySshCredential(username, privateKey, Optional.of(privateKeyFile));
    }

    private final Optional<Path> keyPath;
    private final byte[] privateKey;

    private PublicKeySshCredential(String username, byte[] privateKey, Optional<Path> keyPath) {
        super(username);
        this.privateKey = privateKey.clone();
        this.keyPath = keyPath;
    }

    @Override
    public void authenticate(SshAuthenticator authenticator) throws IOException {
        authenticator.authByPublicKey(this);
    }

    public byte[] getPrivateKey() {
        return privateKey.clone();
    }

    /**
     * Returns the path of the private key used by this credential if it is
     * available. For example, a path may be unavailable if this credential was
     * constructed using an in-memory key.
     */
    public Optional<Path> getKeyPath() {
        return keyPath;
    }
}
