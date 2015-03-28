package com.palantir.giraffe.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.Uninterruptibles;
import com.palantir.giraffe.command.CommandResult;

final class ProcessStreamHandler {

    private static final int NUM_COPIERS = 3;

    public interface ExceptionListener {
        void onException(Throwable t);
    }

    private final CountDownLatch copierLatch;

    private final SharedByteArrayStream stdout;
    private final SharedByteArrayStream stderr;
    private final SharedByteArrayStream stdin;

    private final CopyOnWriteArrayList<ExceptionListener> listeners;

    private StreamCopier outCopier;
    private StreamCopier errCopier;
    private StreamCopier inCopier;

    public ProcessStreamHandler() {
        copierLatch = new CountDownLatch(NUM_COPIERS);

        stdout = new SharedByteArrayStream();
        stderr = new SharedByteArrayStream();
        stdin = new SharedByteArrayStream();

        listeners = new CopyOnWriteArrayList<>();
    }

    public InputStream getOutput() {
        return stdout.getInputStream();
    }

    public InputStream getError() {
        return stderr.getInputStream();
    }

    public OutputStream getInput() {
        return stdin.getOutputStream();
    }

    public void addListener(ExceptionListener listener) {
        listeners.add(listener);
    }

    public void startCopy(HandlableProcess process, Executor executor) {
        outCopier = new StreamCopier(process.getOutput(), stdout.getOutputStream());
        errCopier = new StreamCopier(process.getError(), stderr.getOutputStream());
        inCopier = new StreamCopier(stdin.getInputStream(), process.getInput(), true);

        submitCopier(outCopier, executor);
        submitCopier(errCopier, executor);
        submitCopier(inCopier, executor);
    }

    /**
     * Waits for copying to finsh and closes internal buffers. This method does
     * not close the source streams.
     */
    public void finishCopy() {
        // close write() side of stdin to unblock copier
        stdin.getOutputStream().close();

        // wait for copier threads to exit
        Uninterruptibles.awaitUninterruptibly(copierLatch);

        // close output after we know all data is copied
        stdout.close();
        stderr.close();
    }

    public CommandResult toResult(int exitStatus, Charset charset) {
        String stdOut = new String(stdout.readRemainingData(), charset);
        String stdErr = new String(stderr.readRemainingData(), charset);
        return new CommandResult(exitStatus, stdOut, stdErr);
    }

    private void submitCopier(StreamCopier copier, Executor executor) {
        ListenableFutureTask<Void> task = ListenableFutureTask.create(copier);
        Futures.addCallback(task, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                copierLatch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                copierLatch.countDown();
                for (ExceptionListener listener : listeners) {
                    listener.onException(t);
                }
            }
        });
        executor.execute(task);
    }
}
