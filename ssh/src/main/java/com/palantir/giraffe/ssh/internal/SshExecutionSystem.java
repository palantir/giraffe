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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.palantir.giraffe.command.ClosedExecutionSystemException;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.file.FileSystemConvertible;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.internal.CommandFutureTask;

final class SshExecutionSystem extends ExecutionSystem implements FileSystemConvertible {

    private final AtomicBoolean closed;
    private final SshExecutionSystemProvider provider;
    private final URI uri;
    private final SharedSshClient client;
    private final Logger logger;
    private final ExecutorService executor;

    protected SshExecutionSystem(SshExecutionSystemProvider provider, SshSystemContext context) {
        this.provider = provider;
        this.uri = context.getUri();
        this.client = context.getClient();
        this.logger = HostLogger.create(context.getLogger(), Host.fromUri(uri));

        closed = new AtomicBoolean();
        executor = Executors.newCachedThreadPool();
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
        if (closed.compareAndSet(false, true)) {
            client.close();
            executor.shutdownNow();
        }
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public FileSystem asFileSystem() throws IOException {
        if (client.addUser()) {
            Map<String, ?> env = SshEnvironments.makeEnv(client);
            return FileSystems.newFileSystem(uri, env, getClass().getClassLoader());
        } else {
            throw new ClosedExecutionSystemException();
        }
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
        return new SshCommandFuture(command, context, client.getClient(), executor);
    }
}
