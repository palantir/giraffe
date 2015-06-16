package com.palantir.giraffe.command;

import java.nio.file.Path;

import org.junit.rules.ExternalResource;

import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Provides access to the local execution system system for testing.
 *
 * @author bkeyes
 */
public class LocalExecutionSystemRule extends ExternalResource implements ExecutionSystemTestRule {

    private final Path testFilesRoot;

    public LocalExecutionSystemRule(Path testFilesRoot) {
        this.testFilesRoot = testFilesRoot;
    }

    @Override
    public Path getTestFilesRoot() {
        return testFilesRoot;
    }

    @Override
    public Command.Builder getCommandBuilder(String executable) {
        return ExecutionSystems.getDefault().getCommandBuilder(executable);
    }

    @Override
    public String name() {
        return "local";
    }

}
