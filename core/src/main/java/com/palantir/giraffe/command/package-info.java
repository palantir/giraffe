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
/**
 * Provides classes and interfaces for local and remote command execution.
 * <p>
 * Executable commands are represented by {@code Command} objects. Each
 * {@code Command} is associated with an {@code ExecutionSystem} that can
 * execute the command on a local or remote host. Each {@code ExecutionSystem}
 * is associated with an {@code ExecutionSystemProvider}, which is responsible
 * for creating systems for different hosts using the same connection method,
 * for instance SSH.
 * <p>
 * Most operations on {@code Command}s are defined in {@code Commands}. As an
 * example:
 * <blockquote><pre>
 * Command ls = Commands.get("ls", "-a", "-l");
 * CommandResult result = Commands.execute(ls);
 * </pre></blockquote>
 * This executes the "ls" command on the local system, returning an object
 * containing the exit code and output.
 *
 * <h3>Local Execution</h3>
 *
 * The local execution system is accessed using the
 * {@link com.palantir.giraffe.command.ExecutionSystems#getDefault() ExecutionSystems.getDefault()}
 * method. There is exactly one instance of the local execution system and
 * it cannot be closed. Because the local execution system is created automatically,
 * it does not accept an environment map for configuration. Instead, the following
 * system properties modify the behavior of the system:
 * <table border=1>
 * <caption>System Properties</caption>
 * <tr>
 * <th>Property</th><th>Description</th>
 * <tr>
 * <td>{@code giraffe.command.local.envWhitelist}</td>
 * <td>
 * A comma-separated list of variable names to keep when creating process environments.
 * If set, all environment variables not in this whitelist will be removed from the
 * default environment of new processes.
 * <p>
 * When using Giraffe in certain applications, such as servers or similar long-lived
 * services, the application processes is started with environment variables that
 * should not be visible to command processes and may even prevent these processes
 * from executing correctly. Setting this property allows users to create a well-defined
 * default environment for command execution.
 * </td>
 * </tr>
 * </table>
 */
package com.palantir.giraffe.command;
