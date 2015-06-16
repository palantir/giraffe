/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Synchronously executes a command with the default context.
     *
     * @param command the command to execute
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with non-zero status
     * @throws IOException if an I/O error occurs while executing the command
     *
     * @see #execute(Command, CommandContext)
     */
    public static CommandResult execute(Command command) throws IOException {
        return execute(command, CommandContext.defaultContext());
    }

    /**
     * Synchronously executes a command with the specified context.
     * <p>
     * This method blocks until the command terminates.
     *
     * @param command the command to execute
     * @param context the {@link CommandContext}
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a status other than
     *         that specified by the {@link CommandContext}
     * @throws IOException if an I/O error occurs while executing the command
     */
    public static CommandResult execute(Command command, CommandContext context)
            throws IOException {
        checkNotNull(command);
        checkNotNull(context);
        return waitFor(executeAsync(command, context));
    }

    /**
     * Synchronously executes a command with the default context and the
     * given timeout.
     *
     * @param command the command to execute
     * @param timeout the maximum time to wait for the command to terminate
     * @param timeUnit the unit of the timeout argument
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a non-zero status
     * @throws IOException if an I/O error occurs while executing the command
     * @throws TimeoutException if the timeout is reached before the command
     *         terminates
     *
     * @see #execute(Command, CommandContext, long, TimeUnit)
     */
    public static CommandResult execute(
            Command command,
            long timeout,
            TimeUnit timeUnit) throws IOException, TimeoutException {
        return execute(command, CommandContext.defaultContext(), timeout, timeUnit);
    }

    /**
     * Synchronously executes a command with the given context and timeout.
     * <p>
     * This method blocks until the command terminates or the timeout is
     * reached. When the timeout is reached, a best-effort attempt is made to
     * cancel the command before throwing a {@code TimeoutException}.
     *
     * @param command the command to execute
     * @param context the {@link CommandContext}
     * @param timeout the maximum time to wait for the command to terminate
     * @param timeUnit the unit of the timeout argument
     *
     * @return the {@linkplain CommandResult result} of executing the command
     *
     * @throws CommandException if the command exits with a status other than
     *         that specified by the {@link CommandContext}
     * @throws IOException if an I/O error occurs while executing the command
     * @throws TimeoutException if the timeout is reached before the command
     *         terminates
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
     * Executes a command asynchronously with the specified context. Returns a
     * {@link CommandFuture} that allows clients to wait for the command to
     * finish and access input and output streams while the command is in
     * progress.
     * <p>
     * Any errors encountered during execution are reported through the returned
     * future. This includes {@link CommandException}s caused by the
     * context's exit status check.
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
     * Waits for the command associated with a {@code CommandFuture} to
     * terminate.
     * <p>
     * This method is uninterruptible; to allow interruption, use
     * {@link CommandFuture#get()}.
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
     * Waits for the command associated with a {@code CommandFuture} to
     * terminate or the timeout to be reached. If the timeout is reached, a
     * best-effort attempt is made to cancel the command before throwing a
     * {@code TimeoutException}.
     * <p>
     * This method is uninterruptible; to allow interruption, use
     * {@link CommandFuture#get(long, TimeUnit)}.
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
