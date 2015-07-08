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

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.palantir.giraffe.host.AuthenticatedSystemRequest;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.ssh.internal.SshUris;

/**
 * Contains information needed to open a {@link HostControlSystem} that uses the
 * SSH protocol.
 *
 * @author bkeyes
 */
public class SshSystemRequest extends AuthenticatedSystemRequest<SshCredential> {

    public static final String PORT_KEY = "port";
    public static final String LOGGER_KEY = "logger";

    private static final String DEFAULT_LOGGER_NAME = "com.palantir.giraffe.ssh";

    public SshSystemRequest(URI uri, SshCredential credential) {
        super(uri, credential);
        SshUris.checkHostUri(uri);

        setDefaults();
    }

    public SshSystemRequest(URI uri, Map<String, ?> env) {
        super(uri, env, SshCredential.class);
        SshUris.checkHostUri(uri);

        setDefaults();
    }

    // only called from constructors
    private void setDefaults() {
        setPort(uri().getPort());
        setLogger(LoggerFactory.getLogger(DEFAULT_LOGGER_NAME));
    }

    public int getPort() {
        return get(PORT_KEY, Integer.class);
    }

    public void setPort(int port) {
        set(PORT_KEY, port);
    }

    public Logger getLogger() {
        return get(LOGGER_KEY, Logger.class);
    }

    public void setLogger(Logger logger) {
        set(LOGGER_KEY, logger);
    }

    public String getUsername() {
        return getCredential().getUsername();
    }
}
