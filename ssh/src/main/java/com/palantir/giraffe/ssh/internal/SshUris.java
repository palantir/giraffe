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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.ssh.AbstractSshCredential;

/**
 * Creates and validates {@code URI}s for SSH file and execution systems.
 *
 * @author bkeyes
 */
public class SshUris {

    private static final String URI_SCHEME = "ssh";

    public static String getUriScheme() {
        return URI_SCHEME;
    }

    public static void checkUri(URI uri) {
        checkNotNull(uri, "uri must be non-null");
        checkArgument(URI_SCHEME.equals(uri.getScheme()), "scheme is not %s", URI_SCHEME);

        // authority == user@host:port
        checkArgument(uri.getAuthority() != null, "missing authority component");
        checkArgument(uri.getUserInfo() != null, "missing user info component");
        checkArgument(uri.getHost() != null, "missing host component");
        checkArgument(uri.getPort() != -1, "missing port component");

        checkArgument("/".equals(uri.getPath()), "path component is not '/'");

        checkArgument(uri.getQuery() == null, "query component present");
        checkArgument(uri.getFragment() == null, "fragment component present");
    }

    public static URI getUri(Host host, int port, AbstractSshCredential credential) {
        try {
            return new URI(URI_SCHEME,
                    credential.getUsername(), host.getHostname(), port,
                    "/", null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private SshUris() {
        throw new UnsupportedOperationException();
    }
}
