package com.palantir.giraffe.command.test;

import java.util.Arrays;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Base class for execution system implementation tests. Run subclasses with
 * {@link com.palantir.giraffe.test.runner.SystemSuite} or as part of a suite
 * that uses this runner.
 *
 * @author bkeyes
 */
public class ExecutionSystemBaseTest {

    private final ExecutionSystemTestRule esRule;

    protected ExecutionSystemBaseTest(ExecutionSystemTestRule esRule) {
        this.esRule = esRule;
    }

    /**
     * Returns a command for an executable that exists in the test files
     * directory.
     */
    protected Command getCommand(String executable, Object... args) {
        String execPath = esRule.getTestFilesRoot().resolve(executable).toString();
        return esRule.getCommandBuilder(execPath).addArguments(Arrays.asList(args)).build();
    }

    /**
     * Returns a command for an executable that exists on the system path.
     */
    protected Command getSystemCommand(String executable, Object... args) {
        return esRule.getCommandBuilder(executable).addArguments(Arrays.asList(args)).build();
    }

    protected ExecutionSystemProvider getProvider() {
        return getCommand("pwd").getExecutionSystem().provider();
    }

    protected ExecutionSystemTestRule getExecutionSystemRule() {
        return esRule;
    }

}
