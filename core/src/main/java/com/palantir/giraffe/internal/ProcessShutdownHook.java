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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

/**
 * A {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} that destroys
 * registered {@link Process} instances when run.
 *
 * @author bkeyes
 */
class ProcessShutdownHook extends Thread {

    private final Object lock = new Object();

    @GuardedBy("lock")
    private List<Process> processes = new ArrayList<>();

    public void register() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    public void unregister() {
        Runtime.getRuntime().removeShutdownHook(this);
    }

    public void addProcess(Process p) {
        synchronized (lock) {
            checkExit();
            processes.add(p);
        }
    }

    public void removeProcess(Process p) {
        synchronized (lock) {
            checkExit();
            processes.remove(p);
        }
    }

    @GuardedBy("lock")
    private void checkExit() {
        if (processes == null) {
            throw new IllegalStateException("hook is executing or finished");
        }
    }

    @Override
    public void run() {
        List<Process> toDestroy;
        synchronized (lock) {
            toDestroy = processes;
            processes = null;
        }

        for (Process p : toDestroy) {
            p.destroy();
        }
    }
}
