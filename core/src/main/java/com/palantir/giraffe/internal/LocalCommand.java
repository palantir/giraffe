package com.palantir.giraffe.internal;

import java.util.Objects;

final class LocalCommand extends AbstractCommand {

    static final class Builder extends AbstractCommand.Builder {
        private final LocalExecutionSystem es;

        Builder(String command, LocalExecutionSystem es) {
            super(command);
            this.es = es;
        }

        @Override
        public LocalCommand build() {
            return new LocalCommand(es, this);
        }
    }

    private final LocalExecutionSystem es;

    private LocalCommand(LocalExecutionSystem es, Builder builder) {
        super(builder);
        this.es = es;
    }

    @Override
    public LocalExecutionSystem getExecutionSystem() {
        return es;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExecutionSystem(), getExecutable(), getArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LocalCommand)) {
            return false;
        } else {
            LocalCommand that = (LocalCommand) obj;
            return getExecutionSystem().equals(that.getExecutionSystem())
                    && getExecutable().equals(that.getExecutable())
                    && getArguments().equals(that.getArguments());
        }
    }

}
