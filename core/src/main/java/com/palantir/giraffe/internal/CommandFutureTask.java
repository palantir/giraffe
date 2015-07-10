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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.AbstractFuture;
import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandException;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.command.TerminatedCommand;

/**
 * An abstract runnable {@link CommandFuture}. Subclasses must implement
 * {@code startProcess()} to start executing a command.
 *
 * @author bkeyes
 */
public abstract class CommandFutureTask extends AbstractFuture<CommandResult>
        implements CommandFuture, RunnableFuture<CommandResult> {

    protected final Command command;
    protected final CommandContext context;

    private final Executor executor;
    private final ProcessStreamHandler handler;

    private final AtomicReference<HandlableProcess> processRef;

    protected CommandFutureTask(Command command, CommandContext context, Executor executor) {
        this.command = command;
        this.context = context;
        this.executor = executor;

        handler = new ProcessStreamHandler(context);
        processRef = new AtomicReference<>();
    }

    @Override
    public InputStream getStdOut() {
        return handler.getOutput();
    }

    @Override
    public InputStream getStdErr() {
        return handler.getError();
    }

    @Override
    public OutputStream getStdIn() {
        return handler.getInput();
    }

    @Override
    public final void run() {
        if (isCancelled()) {
            return;
        }

        try {
            HandlableProcess process = startProcess();
            processRef.set(process);

            if (isCancelled()) {
                // future was cancelled while we started the process
                destroyProcess();
                return;
            }

            handler.addListener(new ExceptionListener());
            handler.startCopy(process, executor);
            int exitStatus = process.waitFor();
            handler.finishCopy();

            try {
                process.closeStreams();
            } catch (IOException ignore) {
                // the process is terminated and the public facing streams are
                // closed, so a failure to close the process streams isn't bad
            }

            CommandResult result = handler.toResult(exitStatus, StandardCharsets.UTF_8);
            if (context.getExitStatusVerifier().apply(exitStatus)) {
                set(result);
            } else {
                TerminatedCommand failed = new TerminatedCommand(command, context, result);
                setException(new CommandException(failed));
            }
        } catch (InterruptedException e) {
            destroyProcess();
            Thread.currentThread().interrupt();
            setException(new IllegalStateException("process thread unexpectedly interrupted", e));
        } catch (Throwable e) {
            setException(e);
            destroyProcess();
        }
    }

    protected abstract HandlableProcess startProcess() throws IOException;

    @Override
    protected void interruptTask() {
        destroyProcess();
    }

    /**
     * Sets {@code process} to {@code null} and calls {@code destroy()} on the
     * previous value if it was non-null. At most one thread will succeed in
     * destroying the process.
     */
    private void destroyProcess() {
        HandlableProcess toDestroy = processRef.getAndSet(null);
        if (toDestroy != null) {
            toDestroy.destroy();
        }
    }

    private final class ExceptionListener implements ProcessStreamHandler.ExceptionListener {
        @Override
        public void onException(Throwable t) {
            // if process is null, it was destroyed and exceptions are expected
            if (processRef.get() != null) {
                setException(new IOException("exception while copying streams ", t));
                destroyProcess();
            }
        }
    }

}
