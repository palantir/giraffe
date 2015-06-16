package com.palantir.giraffe.command;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.palantir.giraffe.SystemPreconditions.checkSameHost;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.Uninterruptibles;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;

/**
 * Static utility methods to create and execute commands.
 * <p>
 * In most cases, the methods defined here delegate to the associated execution
 * system provider to perform the operations.
 *
 * @author bkeyes
 */
public final class Commands {

    private static final int COPY_BUFFER_SIZE = 2048;

    /**
     * Gets a command for the default execution system.
     *
     * @param command the command name
     * @param args the optional arguments
     */
    public static Command get(String command, Object... args) {
        checkNotNull(command);
        checkNotNull(args);
        return getBuilder(command).addArguments(Arrays.asList(args)).build();
    }

    /**
     * Gets a command for the default execution system.
     *
     * @param executable the path to the executable
     * @param args the optional arguments
     *
     * @throws IllegalArgumentException if {@code executable} is not associated
     *         with the default file system
     */
    public static Command get(Path executable, Object... args) {
        checkNotNull(executable);
        checkNotNull(args);
        return getBuilder(executable).addArguments(Arrays.asList(args)).build();
    }

    /**
     * Gets a command builder for the default execution system.
     *
     * @param executable the path to the executable
     *
     * @throws IllegalArgumentException if {@code executable} is not associated
     *         with the default file system
     */
    public static Command.Builder getBuilder(Path executable) {
        checkNotNull(executable);
        checkSameHost(executable, ExecutionSystems.getDefault());
        return getBuilder(executable.toString());
    }

    /**
     * Gets a command builder for the default execution system.
     *
     * @param command the command name
     */
    public static Command.Builder getBuilder(String command) {
        checkNotNull(command);
        return ExecutionSystems.getDefault().getCommandBuilder(command);
    }

    /**
     * Executes a command with the default context, throwing an exception if the
     * exit status is non-zero.
     *
     * @param command the command to execute
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a status other than
     *         that specified by the {@link CommandContext}
     * @throws ClosedExecutionSystemException if the command's execution system
     *         is closed before or during execution
     * @throws IOException if an I/O error occurs while executing the command
     *
     * @see #execute(Command, CommandContext)
     */
    public static CommandResult execute(Command command) throws IOException {
        return execute(command, CommandContext.defaultContext());
    }

    /**
     * Executes a command with the specified context, throwing an exception if
     * the exit status is non-zero. This method blocks until the command exits
     * or the timeout is reached.
     * <p>
     * If a command's execution system is closed while it is executing, a
     * best-effort attempt is made to terminate the command before throwing a
     * {@code ClosedExecutionSystemException}.
     *
     * @param command the command to execute
     * @param context the {@link CommandContext}
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a status other than
     *         that specified by the {@link CommandContext}
     * @throws ClosedExecutionSystemException if the command's execution system
     *         is closed before or during execution
     * @throws IOException if an I/O error occurs while executing the command
     */
    public static CommandResult execute(Command command, CommandContext context)
            throws IOException {
        checkNotNull(command);
        checkNotNull(context);
        return waitFor(executeAsync(command, context));
    }

    /**
     * @param command the command to execute
     * @param timeout the maximum time to time
     * @param timeUnit the time unit of the timout argument
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a non-zero status
     * @throws ClosedExecutionSystemException if the command's execution system
     *         is closed before or during execution
     * @throws IOException if an I/O error occurs while executing the command
     * @throws TimeoutException if the command times out; in this event, the
     *         command execution will also be cancelled as best as possible
     */
    public static CommandResult execute(
            Command command,
            long timeout,
            TimeUnit timeUnit) throws IOException, TimeoutException {
        return execute(command, CommandContext.defaultContext(), timeout, timeUnit);
    }

    /**
     * @param command the command to execute
     * @param context the {@link CommandContext}
     * @param timeout the maximum time to wait (must be at least 0)
     * @param timeUnit the time unit of the timout argument
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a status other than
     *         that specified by the {@link CommandContext}
     * @throws ClosedExecutionSystemException if the command's execution system
     *         is closed before or during execution
     * @throws IOException if an I/O error occurs while executing the command
     * @throws TimeoutException if the command times out; in this event, the
     *         command execution will also be cancelled as best as possible
     */
    public static CommandResult execute(
            Command command,
            CommandContext context,
            long timeout,
            TimeUnit timeUnit) throws IOException, TimeoutException {
        checkNotNull(command);
        checkNotNull(context);
        checkArgument(timeout >= 0, "timeout must be non-negative.");
        checkNotNull(timeUnit);

        return waitFor(executeAsync(command, context), timeout, timeUnit);
    }

    /**
     * Executes a command asynchronously with the default context.
     *
     * @param command the command to execute
     *
     * @return a {@link CommandFuture} for the executing command
     *
     * @see #executeAsync(Command, CommandContext)
     */
    public static CommandFuture executeAsync(Command command) {
        return executeAsync(command, CommandContext.defaultContext());
    }

    /**
     * Executes a command asynchronously with the specified context. This method
     * return a {@link CommandFuture} which allows clients to wait for the
     * command to finish and access the output streams while the command is in
     * progress.
     * <p>
     * Any errors encountered during execution are reported through the returned
     * future. Execution is unverified, meaning the exit status of the command
     * is not checked.
     * <p>
     * If a command's execution system is closed while it is executing, a
     * best-effort attempt is made to terminate the command. If this occurs, the
     * future reports that the command was cancelled. Clients may also cancel
     * the command explicitly using {@link CommandFuture#cancel(boolean)
     * CommandFuture.cancel(true)}.
     *
     * @param command the command to execute
     * @param context the {@link CommandContext}
     *
     * @return a {@link CommandFuture} for the executing command
     */
    public static CommandFuture executeAsync(Command command, CommandContext context) {
        checkNotNull(command);
        checkNotNull(context);
        return provider(command).execute(command, context);
    }

    /**
     * Performs an uninterruptable block on the given {@code CommandFuture}
     * waiting until it completes execution.
     *
     * @param future the {@link CommandFuture} to block on
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command fails by throwing a
     *         {@code CommandException}
     * @throws IOException if an I/O error occurs while executing the command or
     *         the command fails for any other reason.
     */
    public static CommandResult waitFor(CommandFuture future) throws IOException {
        checkNotNull(future);
        try {
            return Uninterruptibles.getUninterruptibly(future);
        } catch (ExecutionException e) {
            throw propagateCause(e);
        }
    }

    /**
     * Performs an uninterruptable block on the given {@code CommandFuture}
     * waiting until it completes execution or the timeout is reached.
     * <p>
     * If the timeout is reached, a best-effort attempt is made to cancel the
     * process before throwing a {@code TimeoutException}.
     *
     * @param future the {@link CommandFuture} to block on
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws TimeoutException if the timeout is reached before the command
     *         terminates
     * @throws CommandException if the command fails by throwing a
     *         {@code CommandException}
     * @throws IOException if an I/O error occurs while executing the command or
     *         the command fails for any other reason.
     */
    public static CommandResult waitFor(CommandFuture future, long timeout, TimeUnit unit)
            throws IOException, TimeoutException {
        checkNotNull(future);
        try {
            return Uninterruptibles.getUninterruptibly(future, timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("timeout waiting for command");
        } catch (ExecutionException e) {
            throw propagateCause(e);
        }
    }

    /**
     * Creates a {@link CommandResult} from the given {@code CommandFuture}.
     * <p>
     * If the future is resolved, this method returns the resolved
     * {@code CommandResult}. Otherwise, a new {@code CommandFuture} is created
     * using the {@linkplain CommandResult#NO_EXIT_STATUS default exit status}
     * and any available output. This method never blocks.
     *
     * @param future the {@link CommandFuture} to create a result from
     *
     * @return a {@code CommandResult}
     *
     * @throws IOException if an error occurs while reading output
     */
    public static CommandResult toResult(CommandFuture future) throws IOException {
        return toResult(future, CommandResult.NO_EXIT_STATUS);
    }

    /**
     * Creates a {@link CommandResult} from the given {@code CommandFuture}.
     * <p>
     * If the future is resolved, this method returns the resolved
     * {@code CommandResult}. Otherwise, a new {@code CommandFuture} is created
     * using the given exit status and any available output. This method never
     * blocks.
     *
     * @param future the {@link CommandFuture} to create a result from
     * @param exitStatus the exit status to use if the future is unresolved
     *
     * @return a {@code CommandResult}
     *
     * @throws IOException if an error occurs while reading output
     */
    public static CommandResult toResult(CommandFuture future, int exitStatus) throws IOException {
        try {
            return future.get(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            // ignore, create result using the streams
        }

        String stdOut = readAvailable(future.getStdOut(), StandardCharsets.UTF_8);
        String stdErr = readAvailable(future.getStdErr(), StandardCharsets.UTF_8);
        return new CommandResult(exitStatus, stdOut, stdErr);
    }

    private static String readAvailable(InputStream is, Charset cs) throws IOException {
        CharBuffer buffer = CharBuffer.allocate(COPY_BUFFER_SIZE);
        StringBuilder data = new StringBuilder();

        InputStreamReader isr = new InputStreamReader(is, cs);
        while (isr.ready()) {
            int r = isr.read(buffer);
            if (r == -1) {
                break;
            }
            buffer.flip();
            data.append(buffer);
        }

        return data.toString();
    }

    private static IOException propagateCause(ExecutionException e) throws IOException {
        Throwable cause = e.getCause();
        if (cause instanceof CommandException) {
            CommandException ce = (CommandException) cause;
            // the stack trace should contain the methods that tried to
            // execute the command instead of the internal methods that
            // actually perform the execution
            ce.fillInStackTrace();
            throw ce;
        }
        // TODO(bkeyes): use a special subclass of IOException?
        throw new IOException("execution failed", cause);
    }

    private static ExecutionSystemProvider provider(Command c) {
        checkNotNull(c);
        return c.getExecutionSystem().provider();
    }

    private Commands() {
        throw new UnsupportedOperationException();
    }
}
