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
package com.palantir.giraffe.ssh;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.palantir.giraffe.SystemUpgrader;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.file.test.FileSystemBaseTest;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;
import com.palantir.giraffe.host.HostControlSystem;

/**
 * Tests that SSH file systems can be converted to execution systems.
 *
 * @author bkeyes
 */
public class FileSystemUpgradeTest extends FileSystemBaseTest {

    public FileSystemUpgradeTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test
    public void createsHostControlSystem() throws IOException, TimeoutException {
        Path root = getFileSystemRule().getTestFilesRoot();
        try (HostControlSystem hcs = SystemUpgrader.upgrade(root.getFileSystem())) {
            assertTrue("execution system is not open", hcs.getExecutionSystem().isOpen());

            // ignore result, as long as this does not fail
            Commands.execute(hcs.getCommand("pwd"), 10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void systemIsIndependent() throws IOException {
        Path root = getFileSystemRule().getTestFilesRoot();
        FileSystem fs = root.getFileSystem();

        assertTrue("file system is not open", fs.isOpen());
        assertTrue(msg(root, "does not exist"), Files.exists(root));

        SystemUpgrader.upgrade(fs).close();

        assertTrue("file system is not open", fs.isOpen());
        assertTrue(msg(root, "does not exist"), Files.exists(root));
    }

}
