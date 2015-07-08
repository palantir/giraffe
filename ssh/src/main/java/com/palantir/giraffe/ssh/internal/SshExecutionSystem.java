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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.palantir.giraffe.command.ClosedExecutionSystemException;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystemUpgradeable;
import com.palantir.giraffe.internal.CommandFutureTask;

import net.schmizz.sshj.SSHClient;

final class SshExecutionSystem extends ExecutionSystem implements HostControlSystemUpgradeable {

    private final SshExecutionSystemProvider provider;
    private final URI uri;
    private final SSHClient client;
    private final Logger logger;
    private final CloseContext closeContext;

    private final ExecutorService executor;

    private SshHostControlSystem sourceSystem;

    protected SshExecutionSystem(SshExecutionSystemProvider provider,
                                 InternalSshSystemRequest request) {
        this.provider = provider;

        this.uri = request.executionSystemUri();
        this.client = request.getClient();
        this.logger = HostLogger.create(request.getLogger(), Host.fromUri(uri));

        closeContext = request.getCloseContext();
        executor = Executors.newCachedThreadPool();

        closeContext.registerCloseable(new Closeable() {
            @Override
            public void close() {
                executor.shutdownNow();
            }
        });
    }

    @Override
    public SshCommand.Builder getCommandBuilder(String command) {
        return new SshCommand.Builder(command, this);
    }

    @Override
    public SshExecutionSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        closeContext.close();
    }

    @Override
    public boolean isOpen() {
        return !closeContext.isClosed();
    }

    @Override
    public URI uri() {
        return uri;
    }

    void setSourceSystem(SshHostControlSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @Override
    public HostControlSystem asHostControlSystem() throws IOException {
        checkOpen();

        assert sourceSystem != null : "source HostControlSystem was never set";
        return sourceSystem.asView();
    }

    protected Logger logger() {
        return logger;
    }

    protected CommandFuture execute(SshCommand command, CommandContext context) {
        CommandFutureTask future = newFutureTask(command, context);
        executor.execute(future);
        return future;
    }

    private CommandFutureTask newFutureTask(SshCommand command, CommandContext context) {
        return new SshCommandFuture(command, context, client, executor);
    }

    private void checkOpen() {
        if (!isOpen()) {
            throw new ClosedExecutionSystemException();
        }
    }
}
