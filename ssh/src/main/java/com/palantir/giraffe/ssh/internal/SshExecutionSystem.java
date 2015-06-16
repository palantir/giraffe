package com.palantir.giraffe.ssh.internal;

import com.palantir.giraffe.ssh.internal.base.BaseSshExecutionSystem;
import com.palantir.giraffe.ssh.internal.base.SshSystemContext;

final class SshExecutionSystem extends BaseSshExecutionSystem<SshCommand> {

    protected SshExecutionSystem(SshExecutionSystemProvider provider, SshSystemContext context) {
        super(provider, context);
    }

    @Override
    public SshCommand.Builder getCommandBuilder(String command) {
        return new SshCommand.Builder(command, this);
    }
}
