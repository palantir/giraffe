package com.palantir.giraffe.ssh.util;

import com.palantir.giraffe.command.Command.Builder;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Connects to a remote SSH server for execution system tests.
 *
 * @author bkeyes
 */
public class RemoteSshExecutionSystemRule extends RemoteSshSystemRule
        implements ExecutionSystemTestRule {

    public RemoteSshExecutionSystemRule(String name, String workingDir) {
        super(name, workingDir);
    }

    @Override
    public Builder getCommandBuilder(String executable) {
        return getHostControlSystem().getExecutionSystem().getCommandBuilder(executable);
    }

}
