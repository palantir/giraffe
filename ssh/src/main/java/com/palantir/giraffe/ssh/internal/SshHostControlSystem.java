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
package com.palantir.giraffe.ssh.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.FileSystems;

import com.palantir.giraffe.command.ExecutionSystems;
import com.palantir.giraffe.host.AbstractHostControlSystem;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.ssh.SshSystemRequest;

import net.schmizz.sshj.DefaultConfig;

/**
 * A {@link HostControlSystem} implementation that uses the SSH protocol.
 *
 * @author bkeyes
 */
public final class SshHostControlSystem extends AbstractHostControlSystem {

    private static final class ConnectionFactoryHolder {
        private static final SshConnectionFactory factory = newFactory();

        private static SshConnectionFactory newFactory() {
            return new SshConnectionFactory(new DefaultConfig());
        }
    }

    public static Builder builder(SshSystemRequest request) throws IOException {
        // even if request has the right type, we want a copy here
        InternalSshSystemRequest internalRequest = new InternalSshSystemRequest(request);
        internalRequest.setSource(SshHostControlSystem.class);

        CloseContext closeContext = new CloseContext();
        internalRequest.setCloseContext(closeContext);

        if (internalRequest.setClientIfMissing(ConnectionFactoryHolder.factory)) {
            closeContext.registerCloseable(internalRequest.getClient(), Integer.MAX_VALUE);
        }

        return new Builder(internalRequest);
    }

    /**
     * Builds {@code SshHostControlSystem} instances.
     */
    public static final class Builder {
        private final InternalSshSystemRequest request;

        private SshFileSystem fs;
        private SshExecutionSystem es;

        private Builder(InternalSshSystemRequest request) {
            this.request = request;
        }

        public Builder setFileSystem(SshFileSystemProvider provider) {
            fs = new SshFileSystem(provider, request);
            return this;
        }

        public Builder setFileSystem() throws IOException {
            fs = (SshFileSystem) FileSystems.newFileSystem(
                    request.fileSystemUri(), request.options(),
                    getClass().getClassLoader());
            return this;
        }

        public Builder setExecutionSystem(SshExecutionSystemProvider provider) {
            es = new SshExecutionSystem(provider, request);
            return this;
        }

        public Builder setExecutionSystem() throws IOException {
            es = (SshExecutionSystem) ExecutionSystems.newExecutionSystem(
                    request.executionSystemUri(), request.options(),
                    getClass().getClassLoader());
            return this;
        }

        public SshHostControlSystem build() {
            checkNotNull(fs, "file system not set");
            checkNotNull(es, "execution system not set");

            try {
                SshHostControlSystem system = new SshHostControlSystem(this);
                fs.setSourceSystem(system);
                es.setSourceSystem(system);
                return system;
            } catch (Throwable t) {
                try {
                    request.getCloseContext().close();
                } catch (IOException e) {
                    t.addSuppressed(e);
                }
                throw t;
            }
        }
    }

    private final CloseContext closeContext;

    private SshHostControlSystem(Builder builder) {
        super(builder.request.uri(), builder.fs, builder.es);
        this.closeContext = builder.request.getCloseContext();
    }

    @Override
    public void close() throws IOException {
        closeContext.close();
    }

    public HostControlSystem asView() {
        return new View(this);
    }

    private static class View extends AbstractHostControlSystem {
        View(SshHostControlSystem source) {
            super(source.uri(), source.getFileSystem(), source.getExecutionSystem());
        }

        @Override
        public void close() {
            // do nothing
        }
    }
}
