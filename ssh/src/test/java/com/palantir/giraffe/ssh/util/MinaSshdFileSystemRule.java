package com.palantir.giraffe.ssh.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Starts and stops an embedded SSH server for file system tests.
 *
 * @author bkeyes
 */
public class MinaSshdFileSystemRule extends MinaSshdSystemRule implements FileSystemTestRule {

    public MinaSshdFileSystemRule(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Path getIncompatiblePath() {
        return Paths.get("local");
    }
}
