package com.palantir.giraffe.ssh.util;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.rules.ExternalResource;

import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystems;
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
        hcs = HostControlSystems.openRemote(server.getHost());
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
