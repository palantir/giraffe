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
package com.palantir.giraffe.file;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests basic functionality of {@link MoreFiles} methods using the local file
 * system.
 *
 * @author bkeyes
 */
public class MoreFilesTest {

    @Rule
    public final TemporaryFolder workingDir = new TemporaryFolder();

    @Test(expected = FileAlreadyExistsException.class)
    public void copyRecursiveFailsIfTargetExists() throws IOException {
        Path source = workingDir.newFile("source").toPath();
        Path target = workingDir.newFile("target").toPath();
        MoreFiles.copyRecursive(source, target);
    }

    @Test(expected = NoSuchFileException.class)
    public void copyRecursiveFailsIfTargetParentMissing() throws IOException {
        Path source = workingDir.newFile("source").toPath();
        Path parent = workingDir.getRoot().toPath().resolve("parent");
        MoreFiles.copyRecursive(source, parent.resolve("target"));
    }

    @Test(expected = FileSystemException.class)
    public void copyRecursiveFailsIfTargetInsideSource() throws IOException {
        Path source = workingDir.newFile("source").toPath();
        Path target = source.resolve("target").relativize(source);
        MoreFiles.copyRecursive(source, target);
    }

}
