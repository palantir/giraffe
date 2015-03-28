package com.palantir.giraffe.ssh.internal.base;

import com.palantir.giraffe.internal.AbstractCommand;

/**
 * Abstract {@code Command} implementation for SSH-based execution systems.
 *
 * @author bkeyes
 *
 * @param <C> the type of the implementing class
 */
public abstract class BaseSshCommand<C extends BaseSshCommand<C>> extends AbstractCommand {

    /**
     * Builds {@code BaseSshCommand} instances.
     *
     * @param <C> the type of the build command
     */
    protected abstract static class Builder<C extends BaseSshCommand<C>>
            extends AbstractCommand.Builder {

        protected Builder(String command) {
            super(command);
        }

        @Override
        public abstract C build();
    }

    private final BaseSshExecutionSystem<C> es;

    protected BaseSshCommand(BaseSshExecutionSystem<C> es, Builder<C> builder) {
        super(builder);
        this.es = es;
    }

    @Override
    public BaseSshExecutionSystem<C> getExecutionSystem() {
        return es;
    }
}
