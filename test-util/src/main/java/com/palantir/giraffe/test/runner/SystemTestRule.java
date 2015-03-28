package com.palantir.giraffe.test.runner;

import java.nio.file.Path;

import org.junit.rules.TestRule;

/**
 * Provides access to a file or execution system implementation for tests.
 *
 * @author bkeyes
 */
public interface SystemTestRule extends TestRule {

    String name();

    Path getTestFilesRoot();

}
