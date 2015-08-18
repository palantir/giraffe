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

import com.palantir.giraffe.host.Authenticator;

/**
 * Implementations of this interface authenticate SSH connections given
 * appropriate credentials.
 * <p>
 * This class participates in the visitor pattern with {@link SshCredential}.
 *
 * @author bkeyes
 */
public interface SshAuthenticator extends Authenticator {

    void authByPassword(SshCredential credential, char[] password) throws IOException;

    void authByPublicKey(SshCredential credential, byte[] privateKey) throws IOException;

    void authByKerberos(SshCredential credential, String realm, String kdcHostname)
            throws IOException;

}
