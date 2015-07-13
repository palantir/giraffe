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

import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

class CommandExceptionMessage {

    private static final int SPACES_PER_LEVEL = 4;

    private static final Function<String, String> ARG_ESCAPER = new Function<String, String>() {
        @Override
        public String apply(String arg) {
            String escaped = arg;
            escaped = escaped.replace("\n", "\\n");
            escaped = escaped.replace("\r", "\\r");
            escaped = escaped.replace("\t", "\\t");
            return '"' + escaped + '"';
        }
    };

    public static String forTimeout(TerminatedCommand failed, long timeout, TimeUnit unit) {
        return new CommandExceptionMessage(failed).addTimeoutString(timeout, unit).toString();
    }

    public static String forExitStatus(TerminatedCommand failed) {
        return new CommandExceptionMessage(failed).addExitString().toString();
    }

    private final Command command;
    private final CommandContext context;
    private final CommandResult result;

    private final StringBuilder msg;

    private CommandExceptionMessage(TerminatedCommand failed) {
        command = failed.command;
        context = failed.context;
        result = failed.result;

        msg = new StringBuilder();
    }

    @Override
    public String toString() {
        return msg.toString();
    }

    private CommandExceptionMessage addTimeoutString(long timeout, TimeUnit unit) {
        msg.append("timed out after ");
        msg.append(timeout).append(' ').append(unit.toString().toLowerCase());
        return addDetails();
    }

    private CommandExceptionMessage addExitString() {
        msg.append("exited with unexpected status ").append(result.getExitStatus());
        return addDetails();
    }

    private CommandExceptionMessage addDetails() {
        addNewLine();

        indent(1).append("executable: ").append(command.getExecutable());
        addNewLine();

        Iterable<String> escapedArgs = Iterables.transform(command.getArguments(), ARG_ESCAPER);
        indent(1).append("arguments: ");
        Joiner.on(", ").appendTo(msg.append('['), escapedArgs).append(']');
        addNewLine();

        if (context.getWorkingDirectory().isPresent()) {
            indent(1).append("working dir: ").append(context.getWorkingDirectory().get());
            addNewLine();
        }

        CommandEnvironment env = context.getEnvironment();
        if (!env.isDefault()) {
            indent(1).append("environment: ").append(env.getBase()).append(' ');
            msg.append("with changes ").append(env.getChanges());
            addNewLine();
        }

        indent(1).append("execution system: ").append(command.getExecutionSystem().uri());
        addNewLine();

        appendOutput("stderr", result.getStdErr());
        addNewLine();
        appendOutput("stdout", result.getStdOut());

        return this;
    }

    private void addNewLine() {
        msg.append(System.lineSeparator());
    }

    private void appendOutput(String name, String output) {
        indent(1).append(name).append(": ");
        if (output.isEmpty()) {
            msg.append("<no output>");
        } else {
            msg.append(output);
        }
    }

    private StringBuilder indent(int level) {
        return msg.append(String.format("%" + level * SPACES_PER_LEVEL + "s", ""));
    }

}
