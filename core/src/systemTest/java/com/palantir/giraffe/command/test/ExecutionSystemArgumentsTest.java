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
