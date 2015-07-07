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

import java.net.URI;
import java.util.List;

import com.palantir.giraffe.host.spi.HostAccessorProvider;
import com.palantir.giraffe.internal.LocalHostAcessorProvider.LocalHostAccessor;
import com.palantir.giraffe.internal.SchemeProviderFinder;

/**
 * Creates {@link HostAccessor} instances.
 *
 * @author bkeyes
 */
public final class HostAccessors {

    private static final class DefaultAccessorHolder {
        private static final LocalHostAccessor instance = defaultInstance();

        private static LocalHostAccessor defaultInstance() {
            return (LocalHostAccessor) newAccessor(new SystemRequest(URI.create("local:///")));
        }
    }

    /**
     * Gets a {@link HostAccessor} for the local host.
     * <p>
     * The {@code HostControlSystem} for the local host is always open; closing
     * it has no effect.
     */
    public static LocalHostAccessor getDefault() {
        return DefaultAccessorHolder.instance;
    }

    /**
     * Gets a new {@link HostAccessor} for given request.
     * <p>
     * This method exists primarily for programmatic access. System
     * implementations are encouraged to provide, and clients are encouraged to
     * use, public named implementations of {@code HostAccessor}.
     *
     * @param request the {@link SystemRequest}
     *
     * @return a new {@link HostAccessor}
     */
    public static HostAccessor newAccessor(SystemRequest request) {
        checkNotNull(request, "request must be non-null");
        String scheme = request.uri().getScheme();

        List<HostAccessorProvider> providers = HostAccessorProvider.installedProviders();
        return new SchemeProviderFinder<>(HostAccessorProvider.class, providers)
                .findOrThrow(scheme)
                .newAccessor(request);

    }

    private HostAccessors() {
        throw new UnsupportedOperationException();
    }
}
