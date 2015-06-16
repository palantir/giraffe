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
package com.palantir.giraffe.command;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * Listener that shuts down an ExecutionSystem after it's been started and all registered
 * CommandFutures have finished.
 *
 * @author jchien
 */
@ThreadSafe
public class CommandExitLatch {

    private ExecutionSystem executionSystem;
    private boolean started = false;

    @GuardedBy("lock")
    private final Set<CommandFuture> futures = new HashSet<CommandFuture>();

    @GuardedBy("lock")
    private Exception exception = null;

    private final Object lock = new Object();

    /**
     * Constructs a new CommandExitLatch to shutdown an ExecutionSystem
     * after all registered CommandFutures have finished.
     *
     * @param futures CommandFutures to register
     */
    public CommandExitLatch(CommandFuture... futures) {
        for (CommandFuture future : futures) {
            register(future);
        }
    }

    /**
     * Starts the Latch. Should only be called through {@link ExecutionSystems}.
     * Immediately closes the ExecutionSystem if all registered listeners have
     * already finished.
     */
    void startMonitoring(ExecutionSystem es) {
        this.executionSystem = es;
        synchronized (lock) {
            started = true;
            if (futures.isEmpty()) {
                close();
            }
        }
    }

    private void close() {
        try {
            executionSystem.close();
        } catch (Exception e) {
            exception = e;
        }
    }

    private void finish(CommandFuture future) {
        synchronized (lock) {
            futures.remove(future);
            if (started && futures.isEmpty()) {
                close();
            }
        }
    }

    /**
     * @return a boolean of whether the ExecutionSystem was closed
     * @throws IOException wrapping any exceptions thrown by closing the ExecutionSystem.
     */
    public boolean isClosed() throws IOException {
        synchronized (lock) {
            if (futures.isEmpty() && exception == null) {
                return true;
            } else if (futures.isEmpty()) {
                throw new IOException("Error closing ExecutionSystem", exception);
            } else {
                return false;
            }
        }
    }

    /**
     * Register a new CommandFuture on the Listener.
     * @param future the CommandFuture to register
     * @throws IllegalStateException if the Listener has already been started.
     */
    public void register(final CommandFuture future) {
        synchronized (lock) {
            if (started) {
                throw new IllegalStateException("Cannot register new commands after " +
                        "ExecutionSystemShutdownListener has started");
            }
            futures.add(future);
            future.addListener(new Runnable() {
                @Override
                public void run() {
                    finish(future);
                }
            }, MoreExecutors.sameThreadExecutor());
        }
    }
}
