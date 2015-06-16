package com.palantir.giraffe.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import com.palantir.giraffe.SystemConverter;
import com.palantir.giraffe.file.FileSystemConvertible;

/**
 * Tests basic functionality of {@link SystemConverter} methods.
 *
 * @author bkeyes
 */
public class SystemConverterTest {

    private abstract static class ConvertibleFileSystem extends FileSystem
            implements ExecutionSystemConvertible {
        // implementation is mocked
    }

    private abstract static class ConvertibleExecutionSystem extends ExecutionSystem
            implements FileSystemConvertible {
        // implementation is mocked
    }

    private ConvertibleFileSystem fs;
    private Path path;

    private ConvertibleExecutionSystem es;
    private Command command;

    @Before
    public void setup() throws IOException {
        fs = mock(ConvertibleFileSystem.class);
        es = mock(ConvertibleExecutionSystem.class);

        path = mock(Path.class);
        command = mock(Command.class);

        when(fs.asExecutionSystem()).thenReturn(es);
        when(es.asFileSystem()).thenReturn(fs);

        when(path.getFileSystem()).thenReturn(fs);
        when(command.getExecutionSystem()).thenReturn(es);
    }

    // --- file system conversion ---

    @Test
    public void convertFileSystem() throws IOException {
        assertEquals("conversion is incorrect", es, SystemConverter.asExecutionSystem(fs));
    }

    @Test
    public void convertPath() throws IOException {
        assertEquals("conversion is incorrect", es, SystemConverter.asExecutionSystem(path));
    }

    @Test
    public void convertLocalFileSystem() throws IOException {
        ExecutionSystem converted = SystemConverter.asExecutionSystem(FileSystems.getDefault());
        assertEquals("conversion is incorrect", ExecutionSystems.getDefault(), converted);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void uncovertableFileSystem() throws IOException {
        SystemConverter.asExecutionSystem(mock(FileSystem.class));
    }

    @Test
    public void fileSystemIsConvertible() {
        assertTrue("system is not convertible", SystemConverter.isConvertible(fs));

        FileSystem defaultFs = FileSystems.getDefault();
        assertTrue("system is not convertible", SystemConverter.isConvertible(defaultFs));
    }

    // --- execution system conversion ---

    @Test
    public void convertExecutionSystem() throws IOException {
        assertEquals("conversion is incorrect", fs, SystemConverter.asFileSystem(es));
    }

    @Test
    public void convertCommand() throws IOException {
        assertEquals("conversion is incorrect", fs, SystemConverter.asFileSystem(command));
    }

    @Test
    public void convertLocalExecutionSystem() throws IOException {
        FileSystem converted = SystemConverter.asFileSystem(ExecutionSystems.getDefault());
        assertEquals("conversion is incorrect", FileSystems.getDefault(), converted);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void uncovertableExecutionSystem() throws IOException {
        SystemConverter.asFileSystem(mock(ExecutionSystem.class));
    }

    @Test
    public void executionSystemIsConvertible() {
        assertTrue("system is not convertible", SystemConverter.isConvertible(es));

        ExecutionSystem defaultEs = ExecutionSystems.getDefault();
        assertTrue("system is not convertible", SystemConverter.isConvertible(defaultEs));
    }
}
