package com.palantir.giraffe.ssh;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.palantir.giraffe.SystemConverter;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.file.test.FileSystemBaseTest;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests that SSH file systems can be converted to execution systems.
 *
 * @author bkeyes
 */
public class FileSystemConversionTest extends FileSystemBaseTest {

    public FileSystemConversionTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test
    public void createsExecutionSystem() throws IOException, TimeoutException {
        Path root = getFileSystemRule().getTestFilesRoot();
        try (ExecutionSystem es = SystemConverter.asExecutionSystem(root)) {
            assertTrue("execution system is not open", es.isOpen());

            // ignore result, as long as this does not fail
            Commands.execute(es.getCommand("pwd"), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void systemIsIndependent() throws IOException {
        Path root = getFileSystemRule().getTestFilesRoot();

        assertTrue("file system is not open", root.getFileSystem().isOpen());
        assertTrue(msg(root, "does not exist"), Files.exists(root));

        SystemConverter.asExecutionSystem(root).close();

        assertTrue("file system is not open", root.getFileSystem().isOpen());
        assertTrue(msg(root, "does not exist"), Files.exists(root));
    }

}
