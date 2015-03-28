package com.palantir.giraffe.file.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import com.palantir.giraffe.file.test.creator.CrudTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of create, read, update, and delete
 * operations.
 *
 * @author jchien
 */
// TODO(bkeyes): should this class use the provider directly?
public final class FileSystemCRUDTest extends FileSystemBaseTest {

    public FileSystemCRUDTest(FileSystemTestRule testSystem) {
        super(testSystem);
    }

    @Test
    public void createsDirectory() throws IOException {
        Path dir = generateUnusedPath(getTestPath("create_directory"));
        Files.createDirectory(dir);

        assertTrue(msg(dir, "does not exist"), Files.exists(dir));
        assertTrue(msg(dir, "is not a directory"), Files.isDirectory(dir));
    }

    @Test(expected = IOException.class)
    public void createNestedDirectoryFails() throws IOException {
        Path root = generateUnusedPath(getTestPath("missing_directory"));
        Files.createDirectory(root.resolve("child"));
    }

    @Test
    public void createsFile() throws IOException {
        Path file = generateUnusedPath(getTestPath("create_file"));
        Files.createFile(file);

        assertTrue(msg(file, "does not exist"), Files.exists(file));
        assertTrue(msg(file, "is not a file"), Files.isRegularFile(file));
    }

    @Test
    public void deletesFile() throws IOException {
        Path file = generateUnusedPath(getTestPath("delete_file"));

        Files.createFile(file);
        assertTrue(msg(file, "does not exist"), Files.exists(file));

        Files.delete(file);
        assertTrue(msg(file, "exists"), Files.notExists(file));
    }

    @Test(expected = NoSuchFileException.class)
    public void deleteMissingFileFails() throws IOException {
        Files.delete(getTestPath("not_exists.txt"));
    }

    @Test
    public void deleteIfExistsMissingFile() throws IOException {
        assertFalse("missing file deleted", Files.deleteIfExists(getTestPath("not_exists.txt")));
    }

    @Test
    public void deletesEmptyDirectory() throws IOException {
        Path dir = generateUnusedPath(getTestPath("delete_directory"));

        Files.createDirectory(dir);
        assertTrue(msg(dir, "is not directory"), Files.isDirectory(dir));

        Files.delete(dir);
        assertTrue(msg(dir, "exists"), Files.notExists(dir));
    }

    @Test(expected = DirectoryNotEmptyException.class)
    public void deleteNonEmptyDirectoryFails() throws IOException {
        Path root = generateUnusedPath(getTestPath("non_empty_directory"));
        Files.createDirectory(root);
        Files.createFile(root.resolve("child.txt"));
        Files.delete(root);
    }

    @Test(expected = DirectoryNotEmptyException.class)
    public void deleteIfExistsNonEmptyDirectoryFails() throws IOException {
        Path root = generateUnusedPath(getTestPath("non_empty_directory"));
        Files.createDirectory(root);
        Files.createFile(root.resolve("child.txt"));
        Files.deleteIfExists(root);
    }

    @Test
    public void readsFile() throws IOException {
        Path file = getTestPath(CrudTestCreator.F_RO_READ);
        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), CrudTestCreator.READ_DATA, actualData);
    }

    @Test(expected = NoSuchFileException.class)
    public void readMissingFileFails() throws IOException {
        OpenOption[] options = { StandardOpenOption.READ };
        Files.newInputStream(generateUnusedPath(getTestPath("not_exists.txt")), options).close();
    }

    @Test
    public void createsAndWritesFile() throws IOException {
        Path file = generateUnusedPath(getTestPath("create_and_write.txt"));

        String data = "I wrote this data";
        write(file, data);
        assertTrue(msg(file, "does not exist"), Files.exists(file));

        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), data, actualData);
    }

    @Test
    public void createsDeleteOnClose() throws IOException {
        OpenOption[] options = {
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.DELETE_ON_CLOSE
        };

        Path file = generateUnusedPath(getTestPath("delete_on_close.txt"));
        try (SeekableByteChannel ch = Files.newByteChannel(file, options)) {
            assertTrue(msg(file, "does not exist"), Files.exists(file));
        }
        assertTrue(msg(file, "exists"), Files.notExists(file));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void createNewWithExistingFails() throws IOException {
        OpenOption[] options = { StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW };
        Path file = getTestPath(CrudTestCreator.F_RO_READ);
        Files.newByteChannel(file, options).close();
    }

    @Test
    public void writeOverwrites() throws IOException {
        OpenOption[] options = { StandardOpenOption.WRITE, StandardOpenOption.CREATE };

        Path file = generateUnusedPath(getTestPath("overwrite.txt"));
        String data = "HelloWorld";
        write(file, data);

        String moreData = "World";
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, options)) {
            w.write(moreData);
        }

        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), "WorldWorld", actualData);
    }

    @Test
    public void writeAppends() throws IOException {
        OpenOption[] options = {
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        };

        Path file = generateUnusedPath(getTestPath("append.txt"));
        String data = "Hello";
        write(file, data);

        String appendData = "World";
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, options)) {
            w.write(appendData);
        }

        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), data + appendData, actualData);
    }

    @Test
    public void writeAppendsEmptyFile() throws IOException {
        OpenOption[] options = {
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        };

        String data = "Hello World";
        Path file = generateUnusedPath(getTestPath("append_empty.txt"));
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, options)) {
            w.write(data);
        }

        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), data, actualData);
    }

    @Test
    public void writeTruncates() throws IOException {
        OpenOption[] options = {
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        };

        Path file = generateUnusedPath(getTestPath("truncate.txt"));
        String data = "HelloWorld";
        write(file, data);

        String moreData = "World";
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, options)) {
            w.write(moreData);
        }

        String actualData = read(file);
        assertEquals(msg(file, " has wrong data"), moreData, actualData);
    }

    private static void write(Path file, String data) throws IOException {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(data);
        }
    }

    private static String read(Path file) throws IOException {
        ByteBuffer bytes = ByteBuffer.wrap(Files.readAllBytes(file));
        return StandardCharsets.UTF_8.decode(bytes).toString();
    }
}
