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

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.palantir.giraffe.command.Command;

/**
 * Abstract base class for {@link Command} implementations.
 *
 * @author bkeyes
 */
public abstract class AbstractCommand implements Command {

    /**
     * Skeletal command builder implementation. {@code AbstractCommand}
     * subclasses should also subclass this class and implement the
     * {@code build()} method.
     */
    protected abstract static class Builder implements Command.Builder {
        private final String executable;
        private final ImmutableList.Builder<String> arguments;

        protected Builder(String executable) {
            this.executable = executable;
            arguments = ImmutableList.builder();
        }

        @Override
        public final Builder addArgument(Object arg) {
            arguments.add(String.valueOf(arg));
            return this;
        }

        @Override
        public final Builder addArguments(Object first, Object second, Object... more) {
            return addArguments(Lists.asList(first, second, more));
        }

        @Override
        public final Builder addArguments(List<?> args) {
            for (Object arg : args) {
                arguments.add(String.valueOf(arg));
            }
            return this;
        }
    }

    private final String executable;
    private final ImmutableList<String> arguments;

    protected AbstractCommand(Builder builder) {
        executable = builder.executable;
        arguments = builder.arguments.build();
    }

    @Override
    public final String getExecutable() {
        return executable;
    }

    @Override
    public final ImmutableList<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        StringBuilder cmd = new StringBuilder(executable).append(' ');
        return Joiner.on(' ').appendTo(cmd, arguments).toString();
    }
}
