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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;

import com.palantir.giraffe.command.ExecutionSystems;
import com.palantir.giraffe.host.AbstractHostControlSystem;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostAccessor;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.SystemRequest;
import com.palantir.giraffe.host.spi.HostAccessorProvider;

/**
 * Provides access to the host running the JVM.
 *
 * @author bkeyes
 */
public class LocalHostAcessorProvider extends HostAccessorProvider {

    private static final String SCHEME = "local";
    static {
        Host.addLocalUriScheme(SCHEME);
    }

    private static final URI SYSTEM_URI = URI.create(SCHEME + ":///");
    private static final LocalHostAccessor INSTANCE = new LocalHostAccessor();

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public LocalHostAccessor newAccessor(SystemRequest request) {
        checkUri(request.uri());
        return INSTANCE;
    }

    private static void checkUri(URI uri) {
        checkArgument(SCHEME.equals(uri.getScheme()), "scheme is not '%s'", SCHEME);
        checkArgument(uri.getAuthority() == null, "authority component present");
        checkArgument("/".equals(uri.getPath()), "path component is not '/'");

        checkArgument(uri.getQuery() == null, "query component present");
        checkArgument(uri.getFragment() == null, "fragment component present");
    }

    /**
     * {@link HostAccessor} for the host running the Java Virtual Machine.
     */
    public static final class LocalHostAccessor implements HostAccessor {
        private final SystemRequest request;

        private LocalHostAccessor() {
            request = new SystemRequest(SYSTEM_URI);
        }

        @Override
        public Host getHost() {
            return Host.localhost();
        }

        @Override
        public HostControlSystem open() {
            return LocalHostControlSystemHolder.instance;
        }

        @Override
        public SystemRequest request() {
            return request;
        }
    }

    // use holder pattern to keep convention of lazily initializing default
    // file and execution systems
    private static final class LocalHostControlSystemHolder {
        private static final HostControlSystem instance = new LocalHostControlSystem();
    }

    private static final class LocalHostControlSystem extends AbstractHostControlSystem {
        private LocalHostControlSystem() {
            super(SYSTEM_URI, FileSystems.getDefault(), ExecutionSystems.getDefault());
        }

        @Override
        public void close() throws IOException {
            // local file and execution systems do not need to be closed
        }
    }
}
