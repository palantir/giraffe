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

import java.nio.file.ProviderNotFoundException;
import java.util.ServiceLoader;

import javax.annotation.CheckForNull;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Finds {@link SchemeIdentifiable} providers by URI scheme.
 *
 * @author bkeyes
 *
 * @param <P> the type of provider
 */
public class SchemeProviderFinder<P extends SchemeIdentifiable> {

    private final Class<P> providerClass;
    private final Iterable<P> providers;

    @CheckForNull
    private ClassLoader fallbackLoader;

    public SchemeProviderFinder(Class<P> providerClass, Iterable<P> providers) {
        this.providerClass = providerClass;
        this.providers = providers;
    }

    public SchemeProviderFinder<P> setFallbackLoader(ClassLoader loader) {
        fallbackLoader = loader;
        return this;
    }

    public Optional<P> find(String scheme) {
        MatchesScheme predicate = new MatchesScheme(scheme);

        Optional<P> provider = Iterables.tryFind(providers, predicate);
        if (!provider.isPresent() && fallbackLoader != null) {
            ServiceLoader<P> loader = ServiceLoader.load(providerClass, fallbackLoader);
            provider = Iterables.tryFind(loader, predicate);
        }

        return provider;
    }

    public P findOrThrow(String scheme) {
        Optional<P> provider = find(scheme);
        if (provider.isPresent()) {
            return provider.get();
        }

        // TODO(bkeyes): this is a file system specific exception
        String msg = "No provider for scheme \"" + scheme + "\"";
        throw new ProviderNotFoundException(msg);
    }

    private static final class MatchesScheme implements Predicate<SchemeIdentifiable> {
        private final String scheme;

        MatchesScheme(String scheme) {
            this.scheme = scheme;
        }

        @Override
        public boolean apply(SchemeIdentifiable input) {
            return scheme.equalsIgnoreCase(input.getScheme());
        }
    }

}
