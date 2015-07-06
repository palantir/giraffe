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

import java.net.URI;
import java.util.Map;

/**
 * Contains information needed to open a {@link HostControlSystem} for a host
 * that requires authentication.
 *
 * @author bkeyes
 *
 * @param <C> the type of {@link Credential} used to access the host.
 */
public class AuthenticatedSystemRequest<C extends Credential<?>> extends SystemRequest {

    public static final String CREDENTIAL_KEY = "credential";

    private final Class<? extends C> credentialClass;

    public AuthenticatedSystemRequest(URI uri, C credential) {
        super(uri);
        this.credentialClass = getClass(credential);

        set(CREDENTIAL_KEY, credential);
    }

    public AuthenticatedSystemRequest(URI uri, Map<String, ?> env, Class<C> credentialClass) {
        super(uri);
        this.credentialClass = credentialClass;

        setAll(env);

        // call getCredential() to perform type/existence checks
        getCredential();
    }

    public C getCredential() {
        return get(CREDENTIAL_KEY, credentialClass);
    }

    // safe per specification of Object#getClass()
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> getClass(T instance) {
        return (Class<? extends T>) instance.getClass();
    }
}
