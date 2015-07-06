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
package com.palantir.giraffe.host;

import java.io.Closeable;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.ExecutionSystem;

/**
 * Combines a file system and an execution system for a local or remote host.
 * <p>
 * {@code HostControlSystem}s are open on creation and should be closed after
 * use. Clients should not close the underlying systems individually.
 *
 * @author jchien
 */
public interface HostControlSystem extends Closeable {

    /**
     * Gets a path on this host's file system.
     *
     * @see FileSystem#getPath
     */
    Path getPath(String first, String... more);

    /**
     * Gets a command on this host's execution system.
     *
     * @see ExecutionSystem#getCommand(String, Object...)
     */
    Command getCommand(String executable, Object... args);

    /**
     * Gets a command on this host's execution system.
     *
     * @see ExecutionSystem#getCommand(Path, Object...)
     */
    Command getCommand(Path executable, Object... args);

    /**
     * Returns this host's {@code FileSystem}. The system is open and should not
     * be closed.
     */
    FileSystem getFileSystem();

    /**
     * Returns this host's {@code ExecutionSystem}. The system is open and
     * should not be closed.
     */
    ExecutionSystem getExecutionSystem();

    /**
     * Returns the {@link Host} controlled by this system.
     */
    Host getHost();

    /**
     * Returns this system's URI.
     */
    URI uri();
}
