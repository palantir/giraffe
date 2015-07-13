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

import java.io.IOException;
import java.net.URI;

import com.palantir.giraffe.command.Command.Builder;
import com.palantir.giraffe.command.ExecutionSystem;

final class LocalExecutionSystem extends ExecutionSystem {

    private final LocalExecutionSystemProvider provider;
    private final ProcessShutdownHook shutdownHook;

    LocalExecutionSystem(LocalExecutionSystemProvider provider) {
        this.provider = provider;
        this.shutdownHook = new ProcessShutdownHook();
        shutdownHook.register();
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalExecutionSystemProvider provider() {
        return provider;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public Builder getCommandBuilder(String executable) {
        return new LocalCommand.Builder(executable, this);
    }

    @Override
    public URI uri() {
        return LocalExecutionSystemProvider.URI;
    }

    ProcessShutdownHook getShutdownHook() {
        return shutdownHook;
    }
}
