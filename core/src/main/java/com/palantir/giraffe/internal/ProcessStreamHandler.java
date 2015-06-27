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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.Uninterruptibles;
import com.palantir.giraffe.command.CommandContext;
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

    public ProcessStreamHandler(CommandContext context) {
        copierLatch = new CountDownLatch(NUM_COPIERS);

        stdout = newStreamWithWindow(context.getStdoutWindowSize());
        stderr = newStreamWithWindow(context.getStderrWindowSize());
        stdin = new SharedByteArrayStream();

        listeners = new CopyOnWriteArrayList<>();
    }

    private static SharedByteArrayStream newStreamWithWindow(Optional<Integer> window) {
        if (window.isPresent()) {
            return new SharedByteArrayStream(window.get());
        } else {
            return new SharedByteArrayStream();
        }
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
        stdout.getOutputStream().close();
        stderr.getOutputStream().close();
    }

    public CommandResult toResult(int exitStatus, Charset charset) {
        String stdOut = new String(stdout.getBufferedData(), charset);
        String stdErr = new String(stderr.getBufferedData(), charset);
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
