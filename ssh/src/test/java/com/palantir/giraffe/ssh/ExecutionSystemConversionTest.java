package com.palantir.giraffe.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.palantir.giraffe.SystemConverter;
import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.test.ExecutionSystemBaseTest;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;
import com.palantir.giraffe.file.MoreFiles;

/**
 * Tests that SSH execution systems can be converted to file systems.
 *
 * @author bkeyes
 */
public class ExecutionSystemConversionTest extends ExecutionSystemBaseTest {

    private static final String PRINTF_DATA = "giraffe";

    public ExecutionSystemConversionTest(ExecutionSystemTestRule esRule) {
        super(esRule);
    }

    @Test
    public void createsFileSystem() throws IOException {
        Command command = getSystemCommand("pwd");
        try (FileSystem fs = SystemConverter.asFileSystem(command)) {
            assertTrue("file system is not open", fs.isOpen());

            // ignore result, as long as this does not fail
            Files.exists(MoreFiles.defaultDirectory(fs));
        }
    }

    @Test
    public void systemIsIndependent() throws IOException, TimeoutException {
        Command printf = getSystemCommand("printf", "%s", PRINTF_DATA);

        assertTrue("execution system is not open", printf.getExecutionSystem().isOpen());
        assertEquals("incorrect output", PRINTF_DATA, getOutput(printf));

        SystemConverter.asFileSystem(printf).close();

        assertTrue("execution system is not open", printf.getExecutionSystem().isOpen());
        assertEquals("incorrect output", PRINTF_DATA, getOutput(printf));
    }

    private static String getOutput(Command c) throws IOException, TimeoutException {
        return Commands.execute(c, 10, TimeUnit.SECONDS).getStdOut();
    }

}
