package com.palantir.giraffe.ssh.internal;

import java.util.Objects;

import com.palantir.giraffe.ssh.internal.base.BaseSshCommand;

final class SshCommand extends BaseSshCommand<SshCommand> {

    static final class Builder extends BaseSshCommand.Builder<SshCommand> {
        private final SshExecutionSystem es;

        protected Builder(String command, SshExecutionSystem es) {
            super(command);
            this.es = es;
        }

        @Override
        public SshCommand build() {
            return new SshCommand(es, this);
        }
    }

    private SshCommand(SshExecutionSystem es, Builder builder) {
        super(es, builder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExecutionSystem(), getExecutable(), getArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SshCommand)) {
            return false;
        } else {
            SshCommand that = (SshCommand) obj;
            return getExecutionSystem().equals(that.getExecutionSystem())
                    && getExecutable().equals(that.getExecutable())
                    && getArguments().equals(that.getArguments());
        }
    }
}
