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

import org.junit.rules.ExternalResource;

import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.test.runner.SystemTestRule;

class MinaSshdSystemRule extends ExternalResource implements SystemTestRule {

    private final Path workingDir;
    private final MinaTestServer server;

    private HostControlSystem hcs;

    protected MinaSshdSystemRule(Path workingDir) {
        this.workingDir = workingDir.toAbsolutePath();
        server = new MinaTestServer(this.workingDir);
    }

    @Override
    protected void before() throws Throwable {
        server.start();
        hcs = server.getHost().open();
    }

    @Override
    protected void after() {
        try {
            hcs.close();
        } catch (IOException e) {
            throw new IllegalStateException("failed to close systems", e);
        } finally {
            server.stop();
        }
    }

    protected HostControlSystem getHostControlSystem() {
        return hcs;
    }

    @Override
    public String name() {
        return "embedded-ssh";
    }

    @Override
    public Path getTestFilesRoot() {
        return getHostControlSystem().getPath(workingDir.toString());
    }
}
