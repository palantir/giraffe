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

import net.schmizz.sshj.SSHClient;

class InternalSshSystemRequest extends SshSystemRequest {

    public static final String SOURCE_KEY = "giraffe.internal.source";
    public static final String CLIENT_KEY = "giraffe.internal.sshClient";
    public static final String CLOSE_CTX_KEY = "giraffe.internal.closeContext";

    public InternalSshSystemRequest(URI uri, Map<String, ?> env) {
        super(SshUris.replaceScheme(uri, SshUris.getHostScheme()), env);
    }

    public InternalSshSystemRequest(SshSystemRequest request) {
        this(request.uri(), request.options());
    }

    public SSHClient getClient() {
        return get(CLIENT_KEY, SSHClient.class);
    }

    public boolean setClientIfMissing(SshConnectionFactory factory) throws IOException {
        if (!contains(CLIENT_KEY)) {
            SSHClient client = factory.newAuthedConnection(this);
            set(CLIENT_KEY, client);
            return true;
        } else {
            // call getClient() for type check
            getClient();
            return false;
        }
    }

    public boolean isInsternalSource() {
        if (contains(SOURCE_KEY)) {
            return get(SOURCE_KEY, Class.class).equals(SshHostControlSystem.class);
        } else {
            return false;
        }
    }

    public void setSource(Class<?> sourceClass) {
        set(SOURCE_KEY, sourceClass);
    }

    public CloseContext getCloseContext() {
        return get(CLOSE_CTX_KEY, CloseContext.class);
    }

    public void setCloseContext(CloseContext context) {
        set(CLOSE_CTX_KEY, context);
    }

    public URI fileSystemUri() {
        return SshUris.replaceScheme(uri(), SshUris.getFileScheme());
    }

    public URI executionSystemUri() {
        return SshUris.replaceScheme(uri(), SshUris.getExecScheme());
    }

}
