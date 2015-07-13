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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
 * Tests that cancelling a command future stops running processes.
 *
 * @author bkeyes
 */
public class ExecutionSystemCancellationTest extends ExecutionSystemBaseTest {

    public ExecutionSystemCancellationTest(ExecutionSystemTestRule esRule) {
        super(esRule);
    }

    @Test
    public void cancelsProcess() throws Exception {
        CommandFuture future = Commands.executeAsync(getCommand(ScriptExtractionCreator.SLEEP_60));

        int pid;
        try (BufferedReader r = new BufferedReader(readStdOut(future))) {
            String line = r.readLine();
            assertNotNull("stream unexpectedly terminated", line);
            pid = Integer.parseInt(line);
        }

        assertTrue("process " + pid + " is not running", isProcessRunning(pid));
        future.cancel(true);

        final long timeout = TimeUnit.SECONDS.toMillis(5);
        final long start = System.currentTimeMillis();
        while (isProcessRunning(pid) && System.currentTimeMillis() - start < timeout) {
            Thread.sleep(250);
        }

        assertFalse("process " + pid + " is running", isProcessRunning(pid));
    }

    private boolean isProcessRunning(int pid) throws IOException {
        Command ps = getSystemCommand("ps", "-p", pid);
        CommandResult result = Commands.execute(ps, CommandContext.ignoreExitStatus());
        return result.getExitStatus() == 0;
    }

    private static Reader readStdOut(CommandFuture future) {
        return new InputStreamReader(future.getStdOut(), StandardCharsets.UTF_8);
    }

}
