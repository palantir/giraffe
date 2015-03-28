package com.palantir.giraffe.command.test.runner;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.test.runner.SystemTestRule;

/**
 * Provides access to an execution system implementation for execution system
 * tests.
 *
 * @author bkeyes
 */
public interface ExecutionSystemTestRule extends SystemTestRule {

    Command.Builder getCommandBuilder(String executable);

}
