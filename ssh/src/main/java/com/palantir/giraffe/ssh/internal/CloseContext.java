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
import java.util.concurrent.atomic.AtomicBoolean;

import com.palantir.giraffe.file.base.CloseableRegistry;

final class CloseContext implements Closeable {

    private final AtomicBoolean closed = new AtomicBoolean();
    private final CloseableRegistry registry = new CloseableRegistry();

    public void registerCloseable(Closeable closeable) {
        registry.register(closeable);
    }

    public void registerCloseable(Closeable closeable, int priority) {
        registry.register(closeable, priority);
    }

    public void unregister(Closeable closeable) {
        registry.unregister(closeable);
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            registry.close();
        }
    }

}
