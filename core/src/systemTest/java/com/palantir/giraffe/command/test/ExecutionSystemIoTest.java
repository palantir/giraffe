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
package com.palantir.giraffe.command.test;

import static org.junit.Assert.assertEquals;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandFuture;
import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.test.creator.ScriptExtractionCreator;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Tests reading output from executed commands.
 *
 * @author bkeyes
 */
public class ExecutionSystemIoTest extends ExecutionSystemBaseTest {

    private static final int LARGE_SIZE = 10 * 1024 * 1024;

    public ExecutionSystemIoTest(ExecutionSystemTestRule esRule) {
        super(esRule);
    }

    @Test
    public void readsOutput() throws Exception {
        Command cmd = getCommand(ScriptExtractionCreator.HELLO_OUTPUT);
        CommandResult result = Commands.execute(cmd, 10, TimeUnit.SECONDS);
        assertEquals("incorrect output", "Hello World", result.getStdOut());
    }

    @Test
    public void readsError() throws Exception {
        Command cmd = getCommand(ScriptExtractionCreator.HELLO_ERROR);
        CommandResult result = Commands.execute(cmd, 10, TimeUnit.SECONDS);
        assertEquals("incorrect output", "Hello World", result.getStdErr());
    }

    @Test
    public void readsExitStatus() throws Exception {
        int exitStatus = 25;

        Command cmd = getCommand(ScriptExtractionCreator.EXIT, exitStatus);
        CommandContext context = CommandContext.ignoreExitStatus();
        CommandResult result = Commands.execute(cmd, context, 10, TimeUnit.SECONDS);
        assertEquals("incorrect status", exitStatus, result.getExitStatus());
    }

    @Test
    public void readsLargeOutput() throws Exception {
        Command cmd = getCommand(ScriptExtractionCreator.STREAM, LARGE_SIZE);

        String output = Commands.execute(cmd, 20, TimeUnit.SECONDS).getStdOut();
        assertEquals("incorrect size", LARGE_SIZE, output.length());
        for (int i = 0; i < LARGE_SIZE; i++) {
            assertEquals("incorrect char at index " + i, '0', output.charAt(i));
        }
    }

    @Test
    public void writesInput() throws Exception {
        String data = "I enjoy long conversations with myself.\n"
                + "Hello. Hello. How are you? How are you?\n";

        Command echo = getCommand(ScriptExtractionCreator.ECHO_MULTILINE, "END");
        CommandFuture future = Commands.executeAsync(echo);
        try (Writer w = new OutputStreamWriter(future.getStdIn(), StandardCharsets.UTF_8)) {
            w.write(data);
            w.write("END\n");
        }

        CommandResult result = Commands.waitFor(future, 10, TimeUnit.SECONDS);
        assertEquals("incorrect output", data, result.getStdOut());
    }

    @Test
    public void writesSmallInput() throws Exception {
        String data = "y\n";

        Command echo = getCommand(ScriptExtractionCreator.ECHO_LINE);
        CommandFuture future = Commands.executeAsync(echo);
        try (Writer w = new OutputStreamWriter(future.getStdIn(), StandardCharsets.UTF_8)) {
            w.write(data);
        }

        CommandResult result = Commands.waitFor(future, 10, TimeUnit.SECONDS);
        assertEquals("incorrect output", data, result.getStdOut());
    }

    @Test
    public void writesSingleCharacter() throws Exception {
        String data = "\n";

        Command echo = getCommand(ScriptExtractionCreator.ECHO_LINE);
        CommandFuture future = Commands.executeAsync(echo);
        try (Writer w = new OutputStreamWriter(future.getStdIn(), StandardCharsets.UTF_8)) {
            w.write(data);
        }

        CommandResult result = Commands.waitFor(future, 10, TimeUnit.SECONDS);
        assertEquals("incorrect output", data, result.getStdOut());
    }

}
