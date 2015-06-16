/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import javax.annotation.CheckForNull;

import com.google.common.util.concurrent.MoreExecutors;
import com.palantir.giraffe.command.CommandFuture;

/**
 * Listens on long-running commands and runs {@link Runnable}s when certain
 * output appears.
 *
 * @author alake
 */
public final class CommandOutputTrigger extends AbstractStreamWatcher {
    private final ResponseProvider<Runnable> callbackMap;
    private final Executor executor;

    /**
     * Create a {@code CommandOutputTrigger} for a running command using the
     * provided {@code ResponseProvider}. By default, the {@code Runnable} obtained
     * from the {@code ResponseProvider} will be executed by the same thread.
     *
     * @param callbackMap The {@code ResponseProvider} to use
     * @param commandExecution The executing command
     */
    public CommandOutputTrigger(ResponseProvider<Runnable> callbackMap,
                                CommandFuture commandExecution) {
        this(callbackMap, commandExecution, MoreExecutors.sameThreadExecutor());
    }

    /**
     * Create a {@code CommandOutputTrigger} for a running command using the
     * provided {@code ResponseProvider}. The {@code Runnable} obtained from the
     * {@code ResponseProvider} will be executed by the provided {@code Executor}.
     *
     * @param callbackMap The {@code ResponseProvider} to use
     * @param commandExecution The executing command
     * @param executor The executor to use for executing the {@code Runnable}s
     */
    public CommandOutputTrigger(ResponseProvider<Runnable> callbackMap,
                                CommandFuture commandExecution,
                                Executor executor) {
        super(commandExecution.getStdOut(), StandardCharsets.UTF_8);
        checkNotNull(callbackMap);
        checkNotNull(executor);
        this.callbackMap = callbackMap;
        this.executor = executor;
    }

    @Override
    protected boolean processToken(String matchedToken) {
        @CheckForNull
        final Runnable callback = callbackMap.lookupResponse(matchedToken);

        if (callback == null) {
            return false;
        } else {
            executor.execute(callback);
            return true;
        }
    }
}
