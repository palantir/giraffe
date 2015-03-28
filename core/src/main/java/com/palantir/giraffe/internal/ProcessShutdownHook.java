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
