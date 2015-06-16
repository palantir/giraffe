package com.palantir.giraffe.command;

import static com.palantir.giraffe.SystemPreconditions.checkSameHost;

import java.io.Closeable;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

import com.palantir.giraffe.command.spi.ExecutionSystemProvider;

/**
 * Provides an interface for command execution and is the factory for command
 * objects.
 *
 * @author bkeyes
 */
public abstract class ExecutionSystem implements Closeable {

    protected ExecutionSystem() {}

    /**
     * Returns this system's {@link ExecutionSystemProvider}.
     */
    public abstract ExecutionSystemProvider provider();

    /**
     * Returns {@code true} if this system is open.
     */
    public abstract boolean isOpen();

    /**
     * Returns this system's URI.
     */
    public abstract URI uri();

    /**
     * Gets a command for this execution system.
     *
     * @param command the command name
     * @param args the optional arguments
     */
    public final Command getCommand(String command, Object... args) {
        return getCommandBuilder(command).addArguments(Arrays.asList(args)).build();
    }

    /**
     * Gets a command for this execution system.
     *
     * @param executable the path to the executable
     * @param args the optional arguments
     *
     * @throws IllegalArgumentException if {@code executable} and this execution
     *         system are associated with different hosts
     */
    public final Command getCommand(Path executable, Object... args) {
        return getCommandBuilder(executable).addArguments(Arrays.asList(args)).build();
    }

    /**
     * Gets a command builder for this execution system.
     *
     * @param executable the path to the executable
     *
     * @throws IllegalArgumentException if {@code executable} and this execution
     *         system are associated with different hosts
     */
    public final Command.Builder getCommandBuilder(Path executable) {
        checkSameHost(executable, this);
        return getCommandBuilder(executable.toString());
    }

    /**
     * Gets a command builder for this execution system.
     *
     * @param command the command name
     */
    public abstract Command.Builder getCommandBuilder(String command);
}
