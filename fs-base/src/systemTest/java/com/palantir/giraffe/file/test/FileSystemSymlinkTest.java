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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.junit.Test;

import com.palantir.giraffe.file.test.creator.SymlinkTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of symlink operations.
 *
 * @author bkeyes
 */
public class FileSystemSymlinkTest extends FileSystemBaseTest {

    public FileSystemSymlinkTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test
    public void brokenSymlinkExists() {
        Path link = getTestPath(SymlinkTestCreator.F_RO_BROKEN_SYMLINK);
        assertTrue(msg(link, "does not exist"), Files.exists(link, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void readsSymlink() throws IOException {
        Path link = getTestPath(SymlinkTestCreator.F_RO_SYMLINK);
        Path read = Files.readSymbolicLink(link);

        Path expectedTarget = getTestPath(SymlinkTestCreator.F_RO_TARGET);
        assertEquals(msg(link, "read incorrectly"), expectedTarget, read);
    }

    @Test
    public void createsSymlink() throws IOException {
        Path target = getTestPath(SymlinkTestCreator.F_RO_TARGET);
        Path link = generateUnusedPath(getTestPath("create_symlink.txt"));

        Files.createSymbolicLink(link, target);

        Path read = Files.readSymbolicLink(link);
        assertEquals(msg(link, "read incorrectly"), target, read);
    }

    @Test
    public void deleteSymlinkPreservesFile() throws IOException {
        Path target = generateUnusedPath(getTestPath("delete_symlink_target.txt"));
        Path link = generateUnusedPath(getTestPath("delete_symlink.txt"));

        Files.createFile(target);
        assertTrue(msg(target, "does not exist"), Files.exists(target));

        Files.createSymbolicLink(link, target);
        assertTrue(msg(link, "does not exist"), Files.exists(link, LinkOption.NOFOLLOW_LINKS));

        Files.delete(link);
        assertTrue(msg(link, "exists"), Files.notExists(link, LinkOption.NOFOLLOW_LINKS));
        assertTrue(msg(target, "does not exist"), Files.exists(target));
    }

    @Test
    public void deleteFilePreservesSymlink() throws IOException {
        Path target = generateUnusedPath(getTestPath("delete_file_symlink_target.txt"));
        Path link = generateUnusedPath(getTestPath("delete_file_symlink.txt"));

        Files.createFile(target);
        assertTrue(msg(target, "does not exist"), Files.exists(target));

        Files.createSymbolicLink(link, target);
        assertTrue(msg(link, "does not exist"), Files.exists(link, LinkOption.NOFOLLOW_LINKS));

        Files.delete(target);
        assertTrue(msg(target, "exists"), Files.notExists(target));
        assertTrue(msg(link, "does not exist"), Files.exists(link, LinkOption.NOFOLLOW_LINKS));
    }

    // @Test
    // public void moveSymlink() throws IOException;

    // @Test
    // public void copySymlink() throws IOException;

}
