package com.palantir.giraffe.command;

import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link ListenableFuture} that provides access to the output and exit status
 * of an asynchronous command.
 * <p>
 * This future is resolved when its command terminates, either successfully or
 * with an error. If the command completes successfully, {@link #get() get}
 * returns a {@link CommandResult} containing the exit status and any
 * <em>unread</em> content from the output and error streams. If the command
 * terminates with an error, {@code get} throws an {@code ExecutionException}
 * whose cause is either an {@code IOException} or a runtime exception.
 * <p>
 * Calling {@link #cancel(boolean) cancel(true)} makes a best-effort attempt to
 * terminate a running command. The exact behavior of this method is
 * system-dependent.
 * <p>
 * Clients can read output from or send input to the command process at any time
 * before the process terminates using the streams provided by this future.
 * Closing these streams has no effect on the process and they are closed
 * automatically when the process terminates.
 *
 * @author bkeyes
 */
public interface CommandFuture extends ListenableFuture<CommandResult> {

    /**
     * Returns the command process's standard output stream.
     * <p>
     * Closing this stream has no effect on the process and it is closed
     * automatically when the process terminates. Any content read from this
     * stream will not be available in the {@code CommandResult} returned by
     * this future.
     */
    InputStream getStdOut();

    /**
     * Returns the command process's standard error stream.
     * <p>
     * Closing this stream has no effect on the process and it is closed
     * automatically when the process terminates. Any content read from this
     * stream will not be available in the {@code CommandResult} returned by
     * this future.
     */
    InputStream getStdErr();

    /**
     * Returns the command process's standard input stream.
     * <p>
     * Closing this stream has no effect on the process and it is closed
     * automatically when the process terminates.
     */
    OutputStream getStdIn();
}
