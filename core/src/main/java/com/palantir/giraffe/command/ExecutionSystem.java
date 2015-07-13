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
