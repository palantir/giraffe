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

import java.io.IOException;
import java.net.URI;
import java.nio.file.ProviderNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.util.concurrent.MoreExecutors;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;
import com.palantir.giraffe.internal.LocalExecutionSystemProvider;

/**
 * Factory methods for execution systems.
 * <p>
 * The first invocation of any method in this class triggers loading of the
 * default execution system provider, identified by the URI {@code exec:///}. If
 * loading this provider fails for any reason, an unspecified error is thrown.
 * <p>
 * The first invocation of {@link ExecutionSystemProvider#installedProviders()}
 * from either {@link #newExecutionSystem(URI, Map) newExecutionSystem} or
 * {@link #getExecutionSystem(URI) getExecutionSystem} triggers loading of all
 * installed providers. This loading process is described by
 * {@link ExecutionSystemProvider}.
 * <p>
 * This class also defines a factory method that accepts a {@link ClassLoader},
 * enabling loading of providers from non-standard locations. The class loader
 * is only used if no suitable provider is found using the default methods.
 *
 * @author bkeyes
 */
public final class ExecutionSystems {

    private static final class DefaultExecutionSystemHolder {
        private static final ExecutionSystem defaultExecutionSystem = defaultExecutionSystem();

        private static ExecutionSystem defaultExecutionSystem() {
            // TODO(bkeyes): consider allowing clients to replace the default provider
            ExecutionSystemProvider provider = new LocalExecutionSystemProvider();
            return provider.getExecutionSystem(URI.create("exec:///"));
        }
    }

    /**
     * Returns the default execution system for the local machine.
     */
    public static ExecutionSystem getDefault() {
        return DefaultExecutionSystemHolder.defaultExecutionSystem;
    }

    /**
     * Gets an existing execution system with the specified URI.
     *
     * @param uri the URI of the desired system
     *
     * @throws ExecutionSystemNotFoundException if there is no execution system
     *         with the given URI
     */
    public static ExecutionSystem getExecutionSystem(URI uri) {
        String scheme = uri.getScheme();
        return find(scheme, ExecutionSystemProvider.installedProviders()).getExecutionSystem(uri);
    }

    /**
     * Creates a new execution system for the specified URI. The number of
     * execution systems with a given URI that may open at the same time is
     * implementation dependent.
     *
     * @param uri the URI of the desired system
     * @param env a map of implementation-specific parameters required to create
     *        the system
     *
     * @return a new {@code ExecutionSystem}
     *
     * @throws ExecutionSystemAlreadyExistsException if an execution system with
     *         the specified URI already exists and the implementation forbids
     *         duplicate systems
     * @throws IOException is an I/O error occurs while creating and opening the
     *         execution system
     */
    public static ExecutionSystem newExecutionSystem(URI uri, Map<String, ?> env)
            throws IOException {
        return newExecutionSystem(uri, env, null);
    }

    /**
     * Creates a new execution system for the specified URI. The number of
     * execution systems with a given URI that may open at the same time is
     * implementation dependent.
     *
     * @param uri the URI of the desired system
     * @param env a map of implementation-specific parameters required to create
     *        the system
     * @param loader the {@code ClassLoader} to search for providers if no
     *        matching one is found in the default class loader
     *
     * @return a new {@code ExecutionSystem}
     *
     * @throws ExecutionSystemAlreadyExistsException if an execution system with
     *         the specified URI already exists and the implementation forbids
     *         duplicate systems
     * @throws IOException is an I/O error occurs while creating and opening the
     *         execution system
     */
    public static ExecutionSystem newExecutionSystem(URI uri, Map<String, ?> env,
            ClassLoader loader) throws IOException {
        String scheme = uri.getScheme();
        try {
            List<ExecutionSystemProvider> providers = ExecutionSystemProvider.installedProviders();
            return find(scheme, providers).newExecutionSystem(uri, env);
        } catch (ProviderNotFoundException e) {
            if (loader != null) {
                return loadAndFind(scheme, loader).newExecutionSystem(uri, env);
            } else {
                throw e;
            }
        }
    }

    /**
     * Closes any connections the ExecutionSystem uses to execute commands after the
     * CommandFuture finishes, even if other commands are still executing over the connections.
     * This does nothing when called on local command futures.
     *
     * @param commandFuture the command future corresponding to an asynchronous command
     */
    public static void closeAfterCompletion(final ExecutionSystem es,
            CommandFuture commandFuture) {
        commandFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    es.close();
                } catch (IOException e) {
                    //TODO(jchien): Log
                }
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    /**
     * Blocks the registration of any new futures on the ExecutionSystemShutdownListener.
     * Queues a shutdown of the ExecutionSystem, closing any connections used to execute commands.
     * Any non-registered commands still executing will be terminated.
     */
    public static void closeAfterCompletion(ExecutionSystem es,
            CommandExitLatch listener) {
        listener.startMonitoring(es);
    }

    private static ExecutionSystemProvider find(String scheme,
            Iterable<ExecutionSystemProvider> providers) {
        for (ExecutionSystemProvider provider : providers) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider;
            }
        }
        // TODO(bkeyes): this is a file system specific exception
        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    private static ExecutionSystemProvider loadAndFind(String scheme, ClassLoader loader) {
        return find(scheme, ServiceLoader.load(ExecutionSystemProvider.class, loader));
    }

    private ExecutionSystems() {
        throw new UnsupportedOperationException();
    }
}
