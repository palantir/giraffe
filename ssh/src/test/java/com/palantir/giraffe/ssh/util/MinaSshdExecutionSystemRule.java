package com.palantir.giraffe.ssh.util;

import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Starts and stops an embedded SSH server for execution system tests.
 *
 * @author bkeyes
 */
public class MinaSshdExecutionSystemRule extends MinaSshdSystemRule
        implements ExecutionSystemTestRule {

    public MinaSshdExecutionSystemRule(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Command.Builder getCommandBuilder(String executable) {
        return getHostControlSystem().getExecutionSystem().getCommandBuilder(executable);
    }

}
