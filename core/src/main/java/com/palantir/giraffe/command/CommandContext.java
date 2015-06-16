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
package com.palantir.giraffe.command;

import java.nio.file.Path;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.palantir.giraffe.file.UniformPath;

/**
 * Provides command-independent information that influences how commands
 * execute. For example, the context controls the working directory in which
 * commands execute and the values of environment variables.
 * <p>
 * This class provides several static factory methods for creating common
 * contexts. For more complex contexts that set multiple properties, use the
 * {@linkplain #builder() builder}.
 * <p>
 * Instances of this class are immutable.
 *
 * @author bkeyes
 */
public final class CommandContext {

    private static final CommandContext EMPTY_CONTEXT = builder().build();

    /**
     * Builds {@link CommandContext} objects.
     */
    @SuppressWarnings({ "hiding", "checkstyle:hiddenfield" })
    public static final class Builder {

        private CommandEnvironment environment = CommandEnvironment.defaultEnvironment();
        private Predicate<Integer> exitStatusVerifier = Predicates.equalTo(0);
        private Optional<UniformPath> workingDir = Optional.absent();

        private Builder() {
            // use static builder() method externally
        }

        /**
         * Sets the {@linkplain CommandEnvironment environment} for commands
         * executed with this context.
         *
         * @param environment the environment
         *
         * @return this builder
         */
        public Builder environment(CommandEnvironment environment) {
            this.environment = environment;
            return this;
        }

        /**
         * Sets the required exit status for commands executed with this
         * context. If the command exits with a different exit status, a
         * {@link CommandException} is thrown.
         * <p>
         * By default, an exit status of {@code 0} is required.
         *
         * @param status the required exit status
         *
         * @return this builder
         */
        public Builder requireExitStatus(int status) {
            return requireExitStatus(Predicates.equalTo(status));
        }

        /**
         * Allows any exit status for commands executed with this context. When
         * this is set, {@link CommandException} is never thrown.
         *
         * @return this builder
         */
        public Builder ignoreExitStatus() {
            return requireExitStatus(Predicates.<Integer>alwaysTrue());
        }

        /**
         * Indicates what the permissible exit status of a command execution is
         * through a Predicate. If the Predicate returns true, that is implied
         * to mean that the exit status is permissible.
         *
         * @param exitStatusVerifier The Predicate used to verify the exit
         *        status
         *
         * @return this builder
         */
        public Builder requireExitStatus(Predicate<Integer> exitStatusVerifier) {
            this.exitStatusVerifier = exitStatusVerifier;
            return this;
        }

        /**
         * Sets the working directory for commands executed with this context.
         *
         * @param path the working directory path
         *
         * @return this builder
         */
        public Builder workingDirectory(UniformPath path) {
            workingDir = Optional.of(path);
            return this;
        }

        /**
         * Sets the working directory for commands executed with this context.
         * <p>
         * This method is equivalent to calling
         * {@link #workingDirectory(UniformPath)} after converting the path to a
         * {@code UniformPath}.
         *
         * @param path the working directory path
         *
         * @return this builder
         *
         * @see UniformPath#fromPath(Path)
         */
        public Builder workingDirectory(Path path) {
            return workingDirectory(UniformPath.fromPath(path));
        }

        /**
         * Creates a new {@code CommandContext} using the settings configured by
         * this builder. The builder may be reused to create more contexts after
         * calling this method.
         */
        public CommandContext build() {
            return new CommandContext(this);
        }
    }

    private final CommandEnvironment environment;
    private final Predicate<Integer> exitStatusVerifier;
    private final Optional<UniformPath> workingDir;

    private CommandContext(Builder builder) {
        this.environment = builder.environment.copy();
        this.exitStatusVerifier = builder.exitStatusVerifier;
        this.workingDir = builder.workingDir;
    }

    /**
     * Returns a new {@code CommandContext} builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the default context object. The default context uses the default
     * environment, the default working directory, and requires that commands
     * exit with status {@code 0}.
     */
    public static CommandContext defaultContext() {
        return EMPTY_CONTEXT;
    }

    /**
     * Returns a context that ignores the exit status of commands.
     *
     * @see Builder#ignoreExitStatus()
     */
    public static CommandContext ignoreExitStatus() {
        return builder().ignoreExitStatus().build();
    }

    /**
     * Returns a context that requires commands exit with the given status.
     *
     * @param status the required exit status
     *
     * @see Builder#requireExitStatus(int)
     */
    public static CommandContext requireExitStatus(int status) {
        return builder().requireExitStatus(status).build();
    }

    /**
     * Returns a context that sets the given environment before commands
     * execute.
     *
     * @param environment the environment
     *
     * @see Builder#environment(CommandEnvironment)
     */
    public static CommandContext withEnvironment(CommandEnvironment environment) {
        return builder().environment(environment).build();
    }

    /**
     * Returns a context that executes commands in the given directory.
     *
     * @param path the working directory path
     *
     * @see Builder#workingDirectory(UniformPath)
     */
    public static CommandContext workingDirectory(UniformPath path) {
        return builder().workingDirectory(path).build();
    }

    /**
     * Returns a context that executes commands in the given directory.
     *
     * @param path the working directory path
     *
     * @see Builder#workingDirectory(Path)
     */
    public static CommandContext workingDirectory(Path path) {
        return workingDirectory(UniformPath.fromPath(path));
    }

    /**
     * Returns a copy of this context's {@code CommandEnvironment}.
     */
    public CommandEnvironment getEnvironment() {
        return environment.copy();
    }

    /**
     * Returns the predicate this context uses to verify the exit status of
     * commands.
     */
    public Predicate<Integer> getExitStatusVerifier() {
        return exitStatusVerifier;
    }

    /**
     * Returns this context's working directory. If the returned
     * {@code Optional} is not present, this context uses the system-dependent
     * default working directory.
     */
    public Optional<UniformPath> getWorkingDirectory() {
        return workingDir;
    }
}
