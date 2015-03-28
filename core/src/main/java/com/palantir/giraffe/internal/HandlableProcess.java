package com.palantir.giraffe.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A standard process representation for use with {@code ProcessStreamHandler}
 * and {@code CommandFutureTask}.
 *
 * @author bkeyes
 */
public interface HandlableProcess {

    InputStream getOutput();

    InputStream getError();

    OutputStream getInput();

    /**
     * Waits for this process to terminate, returning its exit status.
     *
     * @throws InterruptedException if this thread is interrupted while waiting
     *         for the process to termiante
     * @throws IOException if an I/O error occurs while waiting for the process
     *         to terminate
     */
    int waitFor() throws InterruptedException, IOException;

    /**
     * Forcibly stops this process. Calling this method on a process that has
     * already terminated has no effect.
     */
    void destroy();

    /**
     * Closes this process's streams.
     *
     * @throws IOException if an I/O error occurs while closing the streams
     */
    void closeStreams() throws IOException;
}
