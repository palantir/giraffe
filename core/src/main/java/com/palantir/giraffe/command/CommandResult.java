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

import java.io.Serializable;

/**
 * Contains the output and exit status of a completed command.
 *
 * @author pchen
 */
public final class CommandResult implements Serializable {

    /**
     * Used when the exit status is not available. This may occur when a result
     * is explicitly constructed for a failed {@link CommandFuture} or when the
     * exit status is not returned from an otherwise successful remote process.
     */
    public static final int NO_EXIT_STATUS = 0xDEADBEEF;

    private final int exitStatus;
    private final String stdOut;
    private final String stdErr;

    public CommandResult(int exitStatus, String stdOut, String stdErr) {
        this.exitStatus = exitStatus;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    /**
     * Returns the exit status of the command.
     */
    public int getExitStatus() {
        return exitStatus;
    }

    /**
     * Returns the standard output of the command.
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * Returns the standard error output of the command.
     */
    public String getStdErr() {
        return stdErr;
    }

    @Override
    public String toString() {
        return String.format("CommandResult[exitStatus = %d]", exitStatus);
    }

    private static final long serialVersionUID = 1852846570605367267L;
}
