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
package com.palantir.giraffe.ssh.internal.base;

import java.net.URI;

import org.slf4j.Logger;

/**
 * Contains information required to create new SSH file and execution systems.
 *
 * @author bkeyes
 */
public class SshSystemContext {

    private final URI uri;
    private final SharedSshClient client;
    private final Logger logger;

    public SshSystemContext(URI uri, SharedSshClient client, Logger logger) {
        this.uri = uri;
        this.client = client;
        this.logger = logger;
    }

    public URI getUri() {
        return uri;
    }

    public SharedSshClient getClient() {
        return client;
    }

    public Logger getLogger() {
        return logger;
    }

}
