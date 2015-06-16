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
package com.palantir.giraffe.ssh.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.rules.ExternalResource;

import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystems;
import com.palantir.giraffe.ssh.SshHost;
import com.palantir.giraffe.test.runner.SystemTestRule;

class RemoteSshSystemRule extends ExternalResource implements SystemTestRule {

    public static final String BASE_DIR_PROPERTY = "giraffe.test.baseDir";
    public static final String HOST_PROPERTY = "giraffe.test.host";
    public static final String HOST_KEY_PROPERTY = "giraffe.test.hostKey";

    private final String name;
    private final String workingDir;

    private HostControlSystem hcs;

    protected RemoteSshSystemRule(String name, String workingDir) {
        this.name = name;
        this.workingDir = workingDir;
    }

    @Override
    protected void before() throws Throwable {
        hcs = HostControlSystems.openRemote(getHost());
    }

    private static SshHost<?> getHost() throws IOException {
        String hostSpec = System.getProperty(HOST_PROPERTY);
        if (hostSpec == null) {
            throw new IllegalStateException(HOST_PROPERTY + " is not set");
        }

        String keyPath = System.getProperty(HOST_KEY_PROPERTY);
        if (keyPath == null) {
            throw new IllegalStateException(HOST_KEY_PROPERTY + " is not set");
        }

        int sep = hostSpec.indexOf('@');
        if (sep < 0) {
            throw new IllegalStateException("host specification is malformed: " + hostSpec);
        }

        String username = hostSpec.substring(0, sep);
        String hostname = hostSpec.substring(sep + 1);
        return SshHost.authWithKey(Host.fromHostname(hostname), username, Paths.get(keyPath));
    }

    @Override
    protected void after() {
        try {
            hcs.close();
        } catch (IOException e) {
            throw new IllegalStateException("failed to close systems", e);
        }
    }

    protected HostControlSystem getHostControlSystem() {
        return hcs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path getTestFilesRoot() {
        return getHostControlSystem().getPath(getBaseDir(), workingDir).toAbsolutePath();
    }

    private static String getBaseDir() {
        String dir = System.getProperty(BASE_DIR_PROPERTY);
        if (dir == null) {
            throw new IllegalStateException(BASE_DIR_PROPERTY + " is not set");
        }
        return dir;
    }
}
