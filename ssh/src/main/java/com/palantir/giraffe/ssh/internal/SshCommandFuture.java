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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandEnvironment;
import com.palantir.giraffe.command.CommandEnvironment.BaseEnvironment;
import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.file.UniformPath;
import com.palantir.giraffe.internal.AbstractHandlableProcess;
import com.palantir.giraffe.internal.CommandFutureTask;
import com.palantir.giraffe.internal.HandlableProcess;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Signal;

final class SshCommandFuture extends CommandFutureTask {

    private final SSHClient client;

    SshCommandFuture(SshCommand command,
                     CommandContext context,
                     SSHClient client,
                     Executor executor) {
        super(command, context, executor);
        this.client = client;
    }

    @Override
    protected HandlableProcess startProcess() throws IOException {
        Logger logger = ((SshCommand) command).getExecutionSystem().logger();

        String fullCommand = buildCommandWithContext(command, context);
        logger.debug("executing command: {}", fullCommand);

        Session session = client.startSession();
        try {
            return new SshProcess(session, session.exec(fullCommand), logger);
        } catch (IOException e) {
            session.close();
            throw e;
        }
    }

    private static String buildCommandWithContext(Command cmd, CommandContext context) {
        StringBuilder result = new StringBuilder();

        Optional<UniformPath> workingDirectory = context.getWorkingDirectory();
        CommandEnvironment env = context.getEnvironment();

        if (workingDirectory.isPresent()) {
            result.append("cd").append(' ');
            result.append(escapeString(workingDirectory.get().toString())).append(' ');
            result.append("&&").append(' ');
        }

        if (!env.isDefault()) {
            result.append("env").append(' ');
            if (env.getBase() == BaseEnvironment.EMPTY) {
                result.append("-i").append(' ');
            }

            for (Map.Entry<String, String> entry : env.getChanges().entrySet()) {
                result.append(escapeString(entry.getKey() + '=' + entry.getValue())).append(' ');
            }
        }

        result.append(escapeString(cmd.getExecutable()));
        for (String arg : cmd.getArguments()) {
            result.append(' ').append(escapeString(arg));
        }

        return result.toString();
    }

    /**
     * Single-quote argument, skipping pre-quoted strings and escaping existing
     * single quotes.
     */
    private static String escapeString(String raw) {
        StringBuilder escaped = new StringBuilder();

        int start = 0;
        int index = -1;
        while ((index = raw.indexOf('\'', start)) >= 0) {
            String section = raw.substring(start, index);
            if (!section.isEmpty()) {
                escaped.append('\'').append(section).append('\'');
            }
            escaped.append("\\'");
            start = index + 1;
        }

        // add any remaining input (possibly the whole string)
        if (start < raw.length()) {
            escaped.append('\'').append(raw.substring(start)).append('\'');
        }

        return escaped.toString();
    }

    private static final class SshProcess extends AbstractHandlableProcess {

        private final Session session;
        private final Session.Command command;
        private final Logger logger;

        SshProcess(Session session, Session.Command command, Logger logger) {
            this.session = session;
            this.command = command;
            this.logger = logger;
        }

        @Override
        public InputStream getOutput() {
            return command.getInputStream();
        }

        @Override
        public InputStream getError() {
            return command.getErrorStream();
        }

        @Override
        public OutputStream getInput() {
            return command.getOutputStream();
        }

        @Override
        public int waitFor() throws IOException {
            command.join();
            if (command.getExitStatus() == null) {
                return CommandResult.NO_EXIT_STATUS;
            } else {
                return command.getExitStatus();
            }
        }

        @Override
        public void destroy() {
            if (command.isOpen()) {
                try {
                    command.signal(Signal.TERM);
                    command.signal(Signal.KILL);
                } catch (IOException e) {
                    logger.debug("failed to send SIGTERM/KILL to remote process", e);
                }
            }

            try {
                closeSuppressed(command, session);
            } catch (IOException e) {
                logger.info("failed to destroy remote process; "
                        + "the sever may not support process termination", e);
            }
        }

    }
}
