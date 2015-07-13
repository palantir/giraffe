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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.test.creator.ScriptExtractionCreator;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Tests that various command argument patterns are escaped correctly.
 *
 * @author bkeyes
 */
public class ExecutionSystemArgumentsTest extends ExecutionSystemBaseTest {

    public ExecutionSystemArgumentsTest(ExecutionSystemTestRule esRule) {
        super(esRule);
    }

    @Test
    public void simpleArguments() throws Exception {
        assertArguments("-a", "-f", "path/to/file.txt");
    }

    @Test
    public void handlesSpaces() throws Exception {
        assertArguments("foo", "bar baz");
    }

    @Test
    public void handlesDoubleQuotes() throws Exception {
        assertArguments("-m", "\"HelloWorld\"");
    }

    @Test
    public void handlesSingleQuotes() throws Exception {
        assertArguments("-m", "'HelloWorld'");
    }

    @Test
    public void handlesMixedQuotes() throws Exception {
        assertArguments("-m", "'Hello\"World\"'");
    }

    @Test
    public void handlesQuotesAndSpaces() throws Exception {
        assertArguments("Hello 'World'", "\"Hello\" World");
    }

    @Test
    public void handlesStandardShellCharacters() throws Exception {
        assertArguments("cat", "foo.log", "|", "grep", "-v", "bar", ">", "out.txt", "&");
    }

    private void assertArguments(String...args) throws Exception {
        String[] actual = executePrintArgs(args);
        assertArrayEquals("incorrect arguments", args, actual);
    }

    private String[] executePrintArgs(String... args) throws IOException, TimeoutException {
        Command c = getCommand(ScriptExtractionCreator.PRINT_ARGS, (Object[]) args);
        return Commands.execute(c, 10, TimeUnit.SECONDS).getStdOut().split("(\n|\n\r)");
    }

}
