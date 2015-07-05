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
package com.palantir.giraffe.internal;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.CheckForNull;

import com.google.common.collect.ImmutableList;

/**
 * Loads {@link SchemeIdentifiable} providers using {@link ServiceLoader}.
 *
 * @author bkeyes
 *
 * @param <P> the type of provider
 */
public final class SchemeProviderLoader<P extends SchemeIdentifiable> {

    private final Class<P> providerClass;

    @CheckForNull
    private P defaultProvider;

    public SchemeProviderLoader(Class<P> providerClass) {
        this.providerClass = providerClass;
    }

    public SchemeProviderLoader<P> setDefaultProvider(P provider) {
        defaultProvider = provider;
        return this;
    }

    public ImmutableList<P> loadProviders() {
        ImmutableList.Builder<P> builder = ImmutableList.builder();
        Set<String> schemes = new HashSet<>();

        ServiceLoader<P> loader = ServiceLoader.load(
                providerClass, ClassLoader.getSystemClassLoader());

        if (defaultProvider != null) {
            schemes.add(defaultProvider.getScheme());
            builder.add(defaultProvider);
        }

        for (P provider : loader) {
            if (schemes.add(provider.getScheme())) {
                builder.add(provider);
            }
        }

        return builder.build();
    }

}
