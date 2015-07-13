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
package com.palantir.giraffe.file.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.junit.Test;

import com.palantir.giraffe.file.test.creator.CopyMoveTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests copying and moving files within a signle file system.
 *
 * @author bkeyes
 */
public final class FileSystemCopyMoveTest extends FileSystemBaseTest {

    public FileSystemCopyMoveTest(FileSystemTestRule testSystem) {
        super(testSystem);
    }

    @Test
    public void copiesFile() throws IOException {
        Path src = getTestPath(CopyMoveTestCreator.F_RO_FILE);
        Path dst = generateUnusedPath(getTestPath("copy_file_dst.txt"));

        getProvider().copy(src, dst);
        assertTrue(msg(src, "does not exist"), Files.exists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        assertEquals(msg(src, "has wrong data"), CopyMoveTestCreator.FILE_DATA, read(src));
        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.FILE_DATA, read(dst));
    }

    @Test
    public void movesFile() throws IOException {
        Path src = generateUnusedPath(getTestPath("move_file_src.txt"));
        Path dst = generateUnusedPath(getTestPath("move_file_dst.txt"));

        write(src, CopyMoveTestCreator.FILE_DATA);
        assertTrue(msg(src, "does not exist"), Files.exists(src));

        getProvider().move(src, dst);
        assertTrue(msg(src, "exists"), Files.notExists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.FILE_DATA, read(dst));
    }

    @Test
    public void copiesEmptyDirectory() throws IOException {
        Path src = getTestPath(CopyMoveTestCreator.F_RO_EMPTY_DIR);
        Path dst = generateUnusedPath(getTestPath("copy_empty_dir_dst"));

        getProvider().copy(src, dst);
        assertTrue(msg(src, "does not exist"), Files.exists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));
    }

    @Test
    public void movesEmptyDirectory() throws IOException {
        Path src = generateUnusedPath(getTestPath("move_empty_dir_src"));
        Path dst = generateUnusedPath(getTestPath("move_empty_dir_dst"));

        Files.createDirectory(src);
        assertTrue(msg(src, "does not exist"), Files.exists(src));

        getProvider().move(src, dst);
        assertTrue(msg(src, "exists"), Files.notExists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));
    }

    @Test
    public void copyFileReplaceExisting() throws IOException {
        Path src = getTestPath(CopyMoveTestCreator.F_RO_FILE);
        Path dst = generateUnusedPath(getTestPath("copy_file_existing_dst.txt"));

        Files.createFile(dst);
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        getProvider().copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));
        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.FILE_DATA, read(dst));
    }

    @Test
    public void moveFileReplaceExisting() throws IOException {
        Path src = generateUnusedPath(getTestPath("move_file_existing_src.txt"));
        Path dst = generateUnusedPath(getTestPath("move_file_existing_dst.txt"));

        write(src, CopyMoveTestCreator.FILE_DATA);
        assertTrue(msg(src, "does not exist"), Files.exists(src));

        Files.createFile(dst);
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        getProvider().move(src, dst, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(msg(src, "exists"), Files.notExists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.FILE_DATA, read(dst));
    }

    @Test
    public void copyFileWithAttributes() throws IOException {
        Path src = getTestPath(CopyMoveTestCreator.F_RO_EXEC_FILE);
        Path dst = generateUnusedPath(getTestPath("copy_exec_file_dst.sh"));

        getProvider().copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES);
        assertTrue(msg(src, "does not exist"), Files.exists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        assertEquals(msg(src, "has wrong data"), CopyMoveTestCreator.EXEC_FILE_DATA, read(src));
        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.EXEC_FILE_DATA, read(dst));

        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(dst);
        assertEquals(msg(dst, "has wrong permissions"),
                CopyMoveTestCreator.EXEC_FILE_PERMS, perms);
    }

    @Test
    public void moveFileWithAttributes() throws IOException {
        Path src = generateUnusedPath(getTestPath("move_exec_file_src.sh"));
        Path dst = generateUnusedPath(getTestPath("move_exec_file_dst.sh"));

        write(src, CopyMoveTestCreator.EXEC_FILE_DATA);
        assertTrue(msg(src, "does not exist"), Files.exists(src));
        Files.setPosixFilePermissions(src, CopyMoveTestCreator.EXEC_FILE_PERMS);

        getProvider().move(src, dst, StandardCopyOption.COPY_ATTRIBUTES);
        assertTrue(msg(src, "exists"), Files.notExists(src));
        assertTrue(msg(dst, "does not exist"), Files.exists(dst));

        assertEquals(msg(dst, "has wrong data"), CopyMoveTestCreator.EXEC_FILE_DATA, read(dst));

        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(dst);
        assertEquals(msg(dst, "has wrong permissions"),
                CopyMoveTestCreator.EXEC_FILE_PERMS, perms);
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
