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
import java.net.URI;
import java.util.Map;

import com.palantir.giraffe.ssh.SshSystemRequest;

class InternalSshSystemRequest extends SshSystemRequest {

    public static final String CLIENT_KEY = "ssh-client";

    public InternalSshSystemRequest(URI uri, Map<String, ?> env) {
        super(SshUris.replaceScheme(uri, SshUris.getHostScheme()), env);
    }

    public InternalSshSystemRequest(SshSystemRequest request) {
        this(request.uri(), request.options());
    }

    public void setClientIfMissing(SshConnectionFactory factory) throws IOException {
        if (!contains(CLIENT_KEY)) {
            SharedSshClient client = new SharedSshClient(factory.newAuthedConnection(this));
            set(CLIENT_KEY, client);
        } else {
            // call getClient() for type check
            getClient();
        }
    }

    public SharedSshClient getClient() {
        return get(CLIENT_KEY, SharedSshClient.class);
    }

}
