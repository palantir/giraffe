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
package com.palantir.giraffe.host.spi;

import java.util.List;
import java.util.ServiceLoader;

import com.google.common.collect.ImmutableList;
import com.palantir.giraffe.host.HostAccessor;
import com.palantir.giraffe.host.SystemRequest;
import com.palantir.giraffe.internal.LocalHostAcessorProvider;
import com.palantir.giraffe.internal.SchemeIdentifiable;
import com.palantir.giraffe.internal.SchemeProviderLoader;

/**
 * Service provider class for {@link HostAccessor}s.
 * <p>
 * An host accessor provider is a concrete implementation of this class that has
 * a zero argument constructor. Providers are identified by their {@code URI}
 * {@linkplain #getScheme() scheme}. URI schemes are case-insensitive. The
 * default provider is identified by the URI scheme "local". It provides an
 * accessor for the host running the Java Virtual Machine.
 * <p>
 * Providers are loaded using the standard service-loading functionality
 * implemented by the {@link ServiceLoader} class. Providers are typically
 * installed by adding a JAR file to the application's classpath. The JAR file
 * contains a configuration file named
 * {@code com.palantir.giraffe.host.spi.HostAccessorProvider} in the resource
 * directory {@code META-INF/services}. This file lists one or more fully
 * qualified names of concrete implementations of this class. The order of
 * loaded providers is implementation specific. If two providers have the same
 * URI scheme the most recently loaded duplicate is discarded.
 * <p>
 * All of the methods in this class are safe for use by multiple threads.
 *
 * @author bkeyes
 */
public abstract class HostAccessorProvider implements SchemeIdentifiable {

    private static final class InstalledProvidersHolder {
        private static final ImmutableList<HostAccessorProvider> providers = loadProviders();

        private static ImmutableList<HostAccessorProvider> loadProviders() {
            return new SchemeProviderLoader<>(HostAccessorProvider.class)
                    .setDefaultProvider(new LocalHostAcessorProvider())
                    .loadProviders();
        }
    }

    public static List<HostAccessorProvider> installedProviders() {
        return InstalledProvidersHolder.providers;
    }

    public abstract HostAccessor newAccessor(SystemRequest request);
}
