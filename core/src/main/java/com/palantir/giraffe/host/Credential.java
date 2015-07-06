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
package com.palantir.giraffe.host;

import java.io.IOException;

/**
 * Contains information required to authenticate with a host or service.
 *
 * @author bkeyes
 *
 * @param <A> the type of {@link Authenticator} that accepts this credential
 */
public abstract class Credential<A extends Authenticator> {

    private final Class<A> authenticatorType;

    protected Credential(Class<A> authenticatorType) {
        this.authenticatorType = authenticatorType;
    }

    /**
     * Returns the type of {@link Authenticator} accepted by this credential.
     */
    public final Class<A> getAuthenticatorType() {
        return authenticatorType;
    }

    /**
     * Authenticates the connection managed by the given {@link Authenticator}
     * using this credential.
     * <p>
     * This method participates in the visitor pattern with methods defined by
     * {@code Authenticator} implementations.
     *
     * @param authenticator the {@code Authenticator} for the connection
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs while authenticating the
     *         connection
     */
    public abstract void authenticate(A authenticator) throws IOException;
}
