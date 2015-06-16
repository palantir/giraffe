package com.palantir.giraffe.file.test.runner;

import java.nio.file.Path;

import com.palantir.giraffe.test.runner.SystemTestRule;

/**
 * Provides access to a file system implementation for file system tests.
 *
 * @author bkeyes
 */
public interface FileSystemTestRule extends SystemTestRule {

    Path getIncompatiblePath();

}
