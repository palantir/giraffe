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

import java.io.IOException;
import java.nio.file.FileSystems;

import com.palantir.giraffe.command.ExecutionSystems;

/**
 * Creates standard {@link HostControlSystem} instances.
 *
 * @author bkeyes
 */
public final class HostControlSystems {

    private static final HostControlSystem LOCAL_INSTANCE = new LocalHostControlSystem();

    /**
     * Gets a {@code HostControlSystem} with the default local file and
     * execution systems. The default system is always open; closing it has no
     * effect.
     */
    public static HostControlSystem getDefault() {
        return LOCAL_INSTANCE;
    }

    /**
     * Opens a {@code HostControlSystem} for the given remote host. The returned
     * system is open and should be closed when it is no longer needed.
     *
     * @param host a {@link RemoteHostAccessor} for the host to control
     *
     * @throws IOException if an I/O error occurs while opening the system
     */
    public static HostControlSystem openRemote(RemoteHostAccessor<?> host) throws IOException {
        return host.openHostControlSystem();
    }

    private static final class LocalHostControlSystem extends AbstractHostControlSystem {
        private LocalHostControlSystem() {
            super(Host.localhost(), FileSystems.getDefault(), ExecutionSystems.getDefault());
        }

        @Override
        public void close() throws IOException {
            // local file and execution systems do not need to be closed
        }
    }

    private HostControlSystems() {
        throw new UnsupportedOperationException();
    }
}
