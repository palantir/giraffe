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
package com.palantir.giraffe.command.spi;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.command.ExecutionSystems;

/**
 * Service provider class for execution systems. The methods defined by the
 * {@link com.palantir.giraffe.command.Commands Commands} class will typically
 * delegate to an instance of this class.
 * <p>
 * An execution system provider is a concrete implementation of this class that
 * has a zero argument constructor. Providers are identified by their
 * {@code URI} {@link #getScheme() scheme}. URI schemes are case-insensitive.
 * The default provider is identified by the URI scheme "exec". It creates the
 * {@link ExecutionSystem} that executes commands on the host running the Java
 * Virtual Machine.
 * <p>
 * A provider is a factory for one or more {@link ExecutionSystem} instances.
 * Each system is identified by a {@code URI} with a scheme that matches this
 * provider's scheme. For example, the default execution system is identified by
 * the URI {@code exec:///}. Other providers may define more complex URI schemes
 * that include host information or query strings.
 * <p>
 * Providers are loaded using the standard service-loading functionality
 * implemented by the {@link ServiceLoader} class. Providers are loaded using
 * the system class loader. If the system class loader cannot be found then the
 * extension class loader is used; if there is no extension class loader then
 * the bootstrap class loader is used. Providers are typically installed by
 * adding a JAR file to the application's classpath. The JAR file contains a
 * configuration file named
 * {@code com.palantir.giraffe.command.spi.ExecutionSystemProvider} in the
 * resource directory {@code META-INF/services}. This file lists one or more
 * fully qualified names of concrete implementations of this class. The order of
 * loaded providers is implementation specific. If two providers have the same
 * URI scheme the most recently loaded duplicate is discarded.
 * <p>
 * All of the methods in this class are safe for use by multiple threads.
 *
 * @author bkeyes
 */
public abstract class ExecutionSystemProvider {

    private static final class InstalledProvidersHolder {
        private static final ImmutableList<ExecutionSystemProvider> providers = loadProviders();

        private static ImmutableList<ExecutionSystemProvider> loadProviders() {
            ImmutableList.Builder<ExecutionSystemProvider> builder = ImmutableList.builder();
            Set<String> schemes = new HashSet<>();

            ServiceLoader<ExecutionSystemProvider> loader = ServiceLoader.load(
                    ExecutionSystemProvider.class,
                    ClassLoader.getSystemClassLoader());

            ExecutionSystemProvider defaultProvider = ExecutionSystems.getDefault().provider();
            schemes.add(defaultProvider.getScheme());
            builder.add(defaultProvider);

            for (ExecutionSystemProvider provider : loader) {
                if (schemes.add(provider.getScheme())) {
                    builder.add(provider);
                }
            }

            return builder.build();
        }
    }

    public static List<ExecutionSystemProvider> installedProviders() {
        return InstalledProvidersHolder.providers;
    }

    public abstract String getScheme();

    public abstract ExecutionSystem newExecutionSystem(URI uri, Map<String, ?> env)
            throws IOException;

    public abstract ExecutionSystem getExecutionSystem(URI uri);

    public abstract CommandFuture execute(Command command, CommandContext context);
}
