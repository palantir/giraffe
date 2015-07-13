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

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.ImmutableList;

/**
 * Checked exception thrown when the timeout for a command is exceeded.
 * <p>
 * The {@linkplain #getMessage() message} contains detailed information about
 * the command that ran, the execution system that ran it, and the output of the
 * command until the timeout. This information is also available
 * programmatically via getter methods.
 *
 * @author bkeyes
 */
public class CommandTimeoutException extends TimeoutException {

    private final String executable;
    private final ImmutableList<String> args;
    private final URI uri;
    private final CommandResult result;

    public CommandTimeoutException(TerminatedCommand failed, long timeout, TimeUnit unit) {
        super(CommandExceptionMessage.forTimeout(failed, timeout, unit));

        this.executable = failed.command.getExecutable();
        this.args = failed.command.getArguments();
        this.uri = failed.command.getExecutionSystem().uri();
        this.result = failed.result;
    }

    /**
     * Returns the failed command.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns the arguments of the failed command.
     */
    public ImmutableList<String> getArguments() {
        return args;
    }

    /**
     * Returns the URI of the system that executed the command.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the result of executing the command until the timeout. The exit
     * status of the result is invalid.
     */
    public CommandResult getResult() {
        return result;
    }

    /**
     * Returns a description of the executed command until the time out.
     * <p>
     * The description spans multiple lines and includes the:
     * <ul>
     * <li>Executable name or path</li>
     * <li>Argument list, with each argument surrounded by double quotes</li>
     * <li>Working directory, if not the default</li>
     * <li>Environment, if not the default</li>
     * <li>Execution system URI</li>
     * <li>Contents of the output and error streams</li>
     * </ul>
     *
     * @return a description of the failed command
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    private static final long serialVersionUID = 8500371075158170387L;
}
