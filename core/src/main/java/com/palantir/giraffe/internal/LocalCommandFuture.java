package com.palantir.giraffe.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import com.palantir.giraffe.command.CommandContext;

final class LocalCommandFuture extends CommandFutureTask {

    private final ProcessBuilder processBuilder;
    private final ProcessShutdownHook shutdownHook;

    LocalCommandFuture(LocalCommand command,
                       CommandContext context,
                       ProcessBuilder processBuilder,
                       Executor executor) {
        super(command, context, executor);
        this.processBuilder = processBuilder;
        this.shutdownHook = command.getExecutionSystem().getShutdownHook();
    }

    @Override
    protected HandlableProcess startProcess() throws IOException {
        Process process = processBuilder.start();
        shutdownHook.addProcess(process);
        return new LocalProcess(process, shutdownHook);
    }

    private static final class LocalProcess extends AbstractHandlableProcess {
        private final Process process;
        private final ProcessShutdownHook shutdownHook;

        LocalProcess(Process process, ProcessShutdownHook shutdownHook) {
            this.process = process;
            this.shutdownHook = shutdownHook;
        }

        @Override
        public InputStream getOutput() {
            return process.getInputStream();
        }

        @Override
        public InputStream getError() {
            return process.getErrorStream();
        }

        @Override
        public OutputStream getInput() {
            return process.getOutputStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            int exitStatus = process.waitFor();
            shutdownHook.removeProcess(process);
            return exitStatus;
        }

        @Override
        public void destroy() {
            process.destroy();
            shutdownHook.removeProcess(process);
        }
    }
}
