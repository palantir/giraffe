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
package com.palantir.giraffe;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.host.Host;

/**
 * Simple static methods to verify the state of file and execution systems taken
 * as arguments.
 * <p>
 * When checking hosts, if either object does not provide a method to get the
 * host directly, it is {@linkplain Host#fromUri(java.net.URI) extracted} from
 * the object's URI. For file systems, this is the URI of the current default
 * directory.
 *
 * @author bkeyes
 */
public final class SystemPreconditions {

    /**
     * Ensures that the given path and execution system refer to the same host.
     *
     * @throws IllegalArgumentException if the hosts are not equal
     */
    public static void checkSameHost(Path path, ExecutionSystem es) {
        Host pathHost = Host.fromUri(path.toUri());
        Host esHost = Host.fromUri(es.uri());
        checkArgument(pathHost.equals(esHost),
                "path host (%s) must equal execution system host (%s)",
                pathHost, esHost);
    }

    /**
     * Ensures that the given file system and execution system refer to the same
     * host.
     *
     * @throws IllegalArgumentException if the hosts are not equal
     */
    public static void checkSameHost(FileSystem fs, ExecutionSystem es) {
        Host fsHost = Host.fromUri(fs.getPath("").toUri());
        Host esHost = Host.fromUri(es.uri());
        checkArgument(fsHost.equals(esHost),
                "file system host (%s) must equal execution system host (%s)",
                fsHost, esHost);
    }

    /**
     * Ensures that two paths refer to the same host.
     *
     * @throws IllegalArgumentException if the hosts are not equal
     */
    public static void checkSameHost(Path p1, Path p2) {
        Host p1Host = Host.fromUri(p1.toUri());
        Host p2Host = Host.fromUri(p2.toUri());
        checkArgument(p1Host.equals(p2Host),
                "first path host (%s) must equal second path host (%s)",
                p1Host, p2Host);
    }

    /**
     * Ensures that the given host is the same as the host of the given file
     * system.
     *
     * @throws IllegalArgumentException if the hosts are not equal
     */
    public static void checkSameHost(Host host, FileSystem fs) {
        Host fsHost = Host.fromUri(fs.getPath("").toUri());
        checkArgument(fsHost.equals(host),
                "file system host (%s) must equal %s",
                fsHost, host);
    }

    /**
     * Ensures that the given host is the same as the host of the given
     * execution system.
     *
     * @throws IllegalArgumentException if the hosts are not equal
     */
    public static void checkSameHost(Host host, ExecutionSystem es) {
        Host esHost = Host.fromUri(es.uri());
        checkArgument(esHost.equals(host),
                "execution system host (%s) must equal %s",
                esHost, host);
    }

    /**
     * Ensures that the given file system is open.
     *
     * @throws IllegalArgumentException if the file system is closed
     */
    public static void checkOpen(FileSystem fs) {
        checkArgument(fs.isOpen(), "file system must be open");
    }

    /**
     * Ensures that the given execution system is open.
     *
     * @throws IllegalArgumentException if the execution system is closed
     */
    public static void checkOpen(ExecutionSystem es) {
        checkArgument(es.isOpen(), "executions system must be open");
    }

    private SystemPreconditions() {
        throw new UnsupportedOperationException();
    }
}
