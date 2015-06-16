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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.nio.file.ProviderMismatchException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandEnvironment;
import com.palantir.giraffe.command.CommandEnvironment.BaseEnvironment;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.command.ExecutionSystemAlreadyExistsException;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;
import com.palantir.giraffe.file.UniformPath;
import com.palantir.giraffe.host.Host;

/**
 * Provides access to the execution system on the host running the JVM.
 *
 * @author bkeyes
 */
public final class LocalExecutionSystemProvider extends ExecutionSystemProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LocalExecutionSystemProvider.class);

    private static final String SCHEME = "exec";
    static {
        Host.addLocalUriScheme(SCHEME);
    }

    static final URI URI = java.net.URI.create(SCHEME + ":///");

    private static final String ENV_WHITELIST_PROPERTY = "giraffe.command.local.envWhitelist";

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger id = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("local-exec " + id.getAndIncrement());
            thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.error("Uncaught exception in thread " + t.getName(), e);
                }
            });
            return thread;
        }
    };

    private final LocalExecutionSystem executionSystem;

    public LocalExecutionSystemProvider() {
        executionSystem = new LocalExecutionSystem(this);
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public ExecutionSystem newExecutionSystem(URI uri, Map<String, ?> env) throws IOException {
        checkUri(uri);
        throw new ExecutionSystemAlreadyExistsException();
    }

    @Override
    public ExecutionSystem getExecutionSystem(URI uri) {
        checkUri(uri);
        return executionSystem;
    }

    private void checkUri(URI uri) {
        checkArgument(SCHEME.equals(uri.getScheme()), "scheme is not '%s'", SCHEME);
        checkArgument(uri.getAuthority() == null, "authority component present");
        checkArgument("/".equals(uri.getPath()), "path component is not '/'");

        checkArgument(uri.getQuery() == null, "query component present");
        checkArgument(uri.getFragment() == null, "fragment component present");
    }

    @Override
    public CommandFuture execute(Command command, CommandContext context) {
        LocalCommand cmd = checkCommand(command);

        List<String> commandTokens = new ArrayList<>();
        commandTokens.add(cmd.getExecutable());
        commandTokens.addAll(escapeArguments(cmd.getArguments()));

        ProcessBuilder process = new ProcessBuilder(commandTokens);
        Optional<UniformPath> workingDir = context.getWorkingDirectory();
        if (workingDir.isPresent()) {
            process.directory(workingDir.get().toPath().toFile());
        }
        modifyEnvironment(process, context);

        final ExecutorService executor = Executors.newCachedThreadPool(THREAD_FACTORY);
        LocalCommandFuture future = new LocalCommandFuture(cmd, context, process, executor);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                executor.shutdown();
            }
        }, executor);
        executor.execute(future);

        return future;
    }

    private static void modifyEnvironment(ProcessBuilder process, CommandContext context) {
        CommandEnvironment env = context.getEnvironment();
        Set<String> whitelist = readEnvironmentWhitelist();

        if (env.getBase() == BaseEnvironment.EMPTY) {
            process.environment().clear();
        } else if (whitelist != null) {
            process.environment().keySet().retainAll(whitelist);
        }
        process.environment().putAll(env.getChanges());
    }

    @CheckForNull
    private static Set<String> readEnvironmentWhitelist() {
        String whitelistString = System.getProperty(ENV_WHITELIST_PROPERTY);
        if (whitelistString != null) {
            Set<String> whitelist = new HashSet<>();
            for (String var : whitelistString.split(",")) {
                whitelist.add(var.trim());
            }
            return whitelist;
        } else {
            return null;
        }
    }

    private static List<String> escapeArguments(List<String> args) {
        // only escape arguments on Windows
        if (!OsDetector.isWindows()) {
            return args;
        }

        List<String> escaped = new ArrayList<>();
        for (String arg : args) {
            escaped.add(arg.replace("\"", "\\\""));
        }
        return escaped;
    }

    private static LocalCommand checkCommand(Command c) {
        if (checkNotNull(c, "command must be non-null") instanceof LocalCommand) {
            return (LocalCommand) c;
        } else {
            String type = c.getClass().getName();
            throw new ProviderMismatchException("incompatible with command of type " + type);
        }
    }


}
