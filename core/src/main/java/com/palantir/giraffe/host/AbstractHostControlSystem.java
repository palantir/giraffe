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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.palantir.giraffe.SystemPreconditions.checkSameHost;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.ExecutionSystem;

/**
 * An abstract {@link HostControlSystem} that delegates to a file system
 * instance and an execution system instance.
 *
 * @author jchien
 * @author bkeyes
 */
public abstract class AbstractHostControlSystem implements HostControlSystem {

    private final FileSystem fs;
    private final ExecutionSystem es;
    private final Host host;

    protected AbstractHostControlSystem(Host host, FileSystem fs, ExecutionSystem es) {
        this.host = checkNotNull(host, "host must be non-null");
        this.fs = checkNotNull(fs, "file system must be non-null");
        this.es = checkNotNull(es, "execution system must be non-null");

        checkSameHost(host, fs);
        checkSameHost(host, es);
    }

    @Override
    public final Path getPath(String first, String... more) {
        return fs.getPath(first, more);
    }

    @Override
    public final Command getCommand(String command, Object... args) {
        return es.getCommand(command, args);
    }

    @Override
    public final Command getCommand(Path executable, Object... args) {
        return es.getCommand(executable, args);
    }

    @Override
    public final FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public final ExecutionSystem getExecutionSystem() {
        return es;
    }

    @Override
    public final Host getHost() {
        return host;
    }

    @Override
    public final String getHostname() {
        return host.getHostname();
    }
}
