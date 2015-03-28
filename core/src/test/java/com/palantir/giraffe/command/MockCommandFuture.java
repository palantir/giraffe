/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.AbstractFuture;

/**
 * Class used to mock {@link CommandFuture}. IO streams are provided and
 * termination behavior can be specified.
 *
 * @author alake
 */
public final class MockCommandFuture
        extends AbstractFuture<CommandResult>
        implements CommandFuture {

    private final CountDownLatch getLatch = new CountDownLatch(1);

    private final InputStream stdOut;
    private final InputStream stdErr;
    private final OutputStream stdIn;

    public MockCommandFuture(InputStream stdOut, InputStream stdErr, OutputStream stdIn) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.stdIn = stdIn;
    }

    @Override
    public InputStream getStdOut() {
        return stdOut;
    }

    @Override
    public InputStream getStdErr() {
        return stdErr;
    }

    @Override
    public OutputStream getStdIn() {
        return stdIn;
    }

    @Override
    public CommandResult get() throws InterruptedException, ExecutionException {
        getLatch.countDown();
        return super.get();
    }

    @Override
    public CommandResult get(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException, ExecutionException {
        getLatch.countDown();
        return super.get(timeout, unit);
    }

    public void awaitGet() throws InterruptedException {
        getLatch.await();
    }

    /**
     * This method will asynchronous fail the CommandFuture after a given period
     * of time simulating a scenario where there was an IOException or an error
     * during computation.
     *
     * @param exception The exception to fail the CommandFuture with
     * @param afterThisManyMillis The number of milliseconds to wait before
     *        failing the CommandFuture
     */
    public void failWithException(final Exception exception, long afterThisManyMillis) {
        if (afterThisManyMillis == 0) {
            setException(exception);
            return;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        setException(exception);
                    }
                },
                afterThisManyMillis,
                TimeUnit.MILLISECONDS);
        executor.shutdown();
    }

    /**
     * This method will asynchronously complete the CommandFuture execution after
     * a given period of time with the provided result.
     *
     * @param result The mocked result of the execution
     * @param afterThisManyMillis The number of milliseconds to wait before
     *        completing the CommandFuture
     */
    public void succeed(final CommandResult result, long afterThisManyMillis) {
        if (afterThisManyMillis == 0) {
            set(result);
            return;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        set(result);
                    }
                },
                afterThisManyMillis,
                TimeUnit.MILLISECONDS);
        executor.shutdown();
    }
}
