package com.palantir.giraffe.ssh.internal.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ProviderMismatchException;
import java.util.Map;

import org.slf4j.Logger;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.command.ExecutionSystemNotFoundException;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;

/**
 * Abstract {@code ExecutionSystemProvider} implementation for SSH-based
 * execution systems.
 *
 * @author bkeyes
 *
 * @param <C> the type of command used by the provider's execution systems
 */
public abstract class BaseSshExecutionSystemProvider<C extends BaseSshCommand<C>>
        extends ExecutionSystemProvider {

    private final Class<C> commandClass;

    protected BaseSshExecutionSystemProvider(Class<C> commandClass) {
        this.commandClass = commandClass;
    }

    @Override
    public ExecutionSystem newExecutionSystem(URI uri, Map<String, ?> env) throws IOException {
        checkUri(uri);

        Logger logger = SshEnvironments.getLogger(env);
        SharedSshClient client = SshEnvironments.getClient(env, getConnectionFactory());
        return newExecutionSystem(new SshSystemContext(uri, client, logger));
    }

    protected abstract SshConnectionFactory getConnectionFactory();

    protected abstract BaseSshExecutionSystem<C> newExecutionSystem(SshSystemContext context);

    @Override
    public ExecutionSystem getExecutionSystem(URI uri) {
        checkUri(uri);
        throw new ExecutionSystemNotFoundException(uri.toString());
    }

    protected abstract void checkUri(URI uri);

    @Override
    public CommandFuture execute(Command command, CommandContext context) {
        C cmd = checkCommand(command);
        return cmd.getExecutionSystem().execute(cmd, context);
    }

    private C checkCommand(Command c) {
        if (commandClass.isInstance(checkNotNull(c, "command must be non-null"))) {
            return commandClass.cast(c);
        } else {
            String type = c.getClass().getName();
            throw new ProviderMismatchException("incompatible with command of type " + type);
        }
    }

}
