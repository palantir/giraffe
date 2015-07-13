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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.command.ExecutionSystems;
import com.palantir.giraffe.host.HostAccessors;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystemUpgradeable;

/**
 * Creates {@link HostControlSystem} instances from {@link FileSystem} and
 * {@link ExecutionSystem} instances.
 * <p>
 * Converting one system to another does not affect the original system. The new
 * system is an always-open view of the original system. Calling {@code close()}
 * has no effect and closing the source system closes all views.
 * <p>
 * Where possible, the new view minimizes resource duplication, reusing
 * connections or other state that is safely shared.
 * <p>
 * Not all file systems and execution systems can be upgraded to
 * {@code HostControlSystem}s. If a conversion is not possible, methods in this
 * class throw {@code UnsupportedOperationException}. Upgrading the default
 * systems of either type is always supported. See provider-specific
 * documentation for details about the compatibility of other system
 * implementations.
 *
 * @author bkeyes
 *
 * @see HostControlSystemUpgradeable
 */
public class SystemUpgrader {

    private static final Upgrader<FileSystem> fsUpgrader =
            new Upgrader<>(FileSystems.getDefault());

    private static final Upgrader<ExecutionSystem> esUpgrader =
            new Upgrader<>(ExecutionSystems.getDefault());

    /**
     * Returns an open {@link HostControlSystem} that accesses the same
     * resources as the given execution system. The execution system is not
     * modified.
     * <p>
     * The new system can read and modify the same files that are accessible to
     * commands executed by the execution system. Changes made by one system are
     * visible to the other, given certain system-dependent conditions are true.
     * In particular, no guarantees are made for concurrent modification or
     * modifications from different threads.
     *
     * @param es the execution system
     *
     * @return an open {@code HostControlSystem}
     *
     * @throws IOException if an I/O error occurs while creating the system
     * @throws UnsupportedOperationException if the execution system does not
     *         support upgrades
     */
    public static HostControlSystem upgrade(ExecutionSystem es) throws IOException {
        return esUpgrader.upgrade(es);
    }

    /**
     * Returns an open {@link HostControlSystem} that accesses the same
     * resources as the given file system. The file system is not modified.
     * <p>
     * Commands executed by the new system can read and modify the same files
     * that are accessible by the file system. Changes made by one system are
     * visible to the other, given certain system-dependent conditions are true.
     * In particular, no guarantees are made for concurrent modification or
     * modifications from different threads.
     *
     * @param fs the file system
     *
     * @return an open {@code HostControlSystem}
     *
     * @throws IOException if an I/O error occurs while creating the system
     * @throws UnsupportedOperationException if the file system does not support
     *         upgrades
     */
    public static HostControlSystem upgrade(FileSystem fs) throws IOException {
        return fsUpgrader.upgrade(fs);
    }

    /**
     * Returns {@code true} if the given file system can be upgrade to a host
     * control system.
     */
    public static boolean isUpgradeable(FileSystem fs) {
        return fsUpgrader.isUpgradeable(fs);
    }

    /**
     * Returns {@code true} if the given execution system can be upgraded to a
     * host control system.
     */
    public static boolean isUpgradeable(ExecutionSystem es) {
        return esUpgrader.isUpgradeable(es);
    }

    private static final class Upgrader<S> {
        private final S defaultSystem;

        Upgrader(S defaultSystem) {
            this.defaultSystem = defaultSystem;
        }

        public boolean isUpgradeable(S system) {
            checkNotNull(system, "system must be non-null");
            return defaultSystem.equals(system) || system instanceof HostControlSystemUpgradeable;
        }

        public HostControlSystem upgrade(S system) throws IOException {
            checkNotNull(system, "system must be non-null");
            if (!isUpgradeable(system)) {
                String msg = system.getClass().getName() + " is not upgradeable";
                throw new UnsupportedOperationException(msg);
            }

            if (system.equals(defaultSystem)) {
                return HostAccessors.getDefault().open();
            } else {
                return ((HostControlSystemUpgradeable) system).asHostControlSystem();
            }
        }
    }

    private SystemUpgrader() {
        throw new UnsupportedOperationException();
    }
}
