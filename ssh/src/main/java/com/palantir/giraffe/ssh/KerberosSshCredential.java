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

/**
 * Authenticates SSH connections via GSS-API in a Kerberos environment.
 *
 * @author benh
 */
public class KerberosSshCredential extends SshCredential {

    /**
     * System property to enable Kerberos debug output when set to {@code true}.
     * Note that debug output is verbose and is printed directly to standard
     * out. This is a limitation of Java's Kerberos implementation.
     */
    public static final String DEBUG_PROPERTY = "giraffe.kerberos.debug";

    public static KerberosSshCredential of(String username) {
        return new KerberosSshCredential(username);
    }

    public KerberosSshCredential(String username) {
        super(username);
    }

    @Override
    public void authenticate(SshAuthenticator authenticator) throws IOException {
        authenticator.authByKerberos(this);
    }
}
