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
