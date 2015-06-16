package com.palantir.giraffe.ssh.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Connects to a remote SSH server for file system tests.
 *
 * @author bkeyes
 */
public class RemoteSshFileSystemRule extends RemoteSshSystemRule implements FileSystemTestRule {

    public RemoteSshFileSystemRule(String name, String workingDir) {
        super(name, workingDir);
    }

    @Override
    public Path getIncompatiblePath() {
        return Paths.get("local");
    }

}
