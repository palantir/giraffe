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
import com.palantir.giraffe.ssh.SshCredential;

/**
 * Creates and validates {@code URI}s for SSH file and execution systems.
 *
 * @author bkeyes
 */
// TODO(bkeyes): move this to a public package?
public class SshUris {

    private static final String HOST_SCHEME = "ssh";
    private static final String FILE_SCHEME = "file+ssh";
    private static final String EXEC_SCHEME = "exec+ssh";

    public static String getHostScheme() {
        return HOST_SCHEME;
    }

    public static String getFileScheme() {
        return FILE_SCHEME;
    }

    public static String getExecScheme() {
        return EXEC_SCHEME;
    }

    public static void checkHostUri(URI uri) {
        checkUri(uri, HOST_SCHEME);
    }

    public static void checkFileUri(URI uri) {
        checkUri(uri, FILE_SCHEME);
    }

    public static void checkExecUri(URI uri) {
        checkUri(uri, EXEC_SCHEME);
    }

    private static void checkUri(URI uri, String scheme) {
        checkNotNull(uri, "uri must be non-null");
        checkArgument(scheme.equals(uri.getScheme()), "scheme is not %s", scheme);

        // authority == user@host:port
        checkArgument(uri.getAuthority() != null, "missing authority component");
        checkArgument(uri.getUserInfo() != null, "missing user info component");
        checkArgument(uri.getHost() != null, "missing host component");
        checkArgument(uri.getPort() != -1, "missing port component");

        checkArgument("/".equals(uri.getPath()), "path component is not '/'");

        checkArgument(uri.getQuery() == null, "query component present");
        checkArgument(uri.getFragment() == null, "fragment component present");
    }

    public static URI getHostUri(Host host, int port, SshCredential cred) {
        return getUri(HOST_SCHEME, host, port, cred);
    }

    public static URI getFileUri(Host host, int port, SshCredential cred) {
        return getUri(FILE_SCHEME, host, port, cred);
    }

    public static URI getExecUri(Host host, int port, SshCredential cred) {
        return getUri(EXEC_SCHEME, host, port, cred);
    }

    private static URI getUri(String scheme, Host host, int port, SshCredential cred) {
        try {
            return new URI(scheme, cred.getUsername(), host.getHostname(), port, "/", null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static URI replaceScheme(URI uri, String newScheme) {
        try {
            return new URI(newScheme, uri.getSchemeSpecificPart(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private SshUris() {
        throw new UnsupportedOperationException();
    }
}
