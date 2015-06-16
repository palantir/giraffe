package com.palantir.giraffe.internal;

import java.io.IOException;
import java.net.URI;

import com.palantir.giraffe.command.Command.Builder;
import com.palantir.giraffe.command.ExecutionSystem;

final class LocalExecutionSystem extends ExecutionSystem {

    private final LocalExecutionSystemProvider provider;
    private final ProcessShutdownHook shutdownHook;

    LocalExecutionSystem(LocalExecutionSystemProvider provider) {
        this.provider = provider;
        this.shutdownHook = new ProcessShutdownHook();
        shutdownHook.register();
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalExecutionSystemProvider provider() {
        return provider;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public Builder getCommandBuilder(String executable) {
        return new LocalCommand.Builder(executable, this);
    }

    @Override
    public URI uri() {
        return LocalExecutionSystemProvider.URI;
    }

    ProcessShutdownHook getShutdownHook() {
        return shutdownHook;
    }
}
