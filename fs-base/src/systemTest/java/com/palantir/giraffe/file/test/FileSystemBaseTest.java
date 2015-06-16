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

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Base class for file system implementation tests. Run subclasses with
 * {@link com.palantir.giraffe.test.runner.SystemSuite} or as part of a suite
 * that uses this runner.
 *
 * @author bkeyes
 */
public class FileSystemBaseTest {

    private final FileSystemTestRule fsRule;

    protected FileSystemBaseTest(FileSystemTestRule fsRule) {
        this.fsRule = fsRule;
    }

    protected Path getPath(String first, String... more) {
        return fsRule.getTestFilesRoot().getFileSystem().getPath(first, more);
    }

    protected Path getTestPath(String first, String... more) {
        return fsRule.getTestFilesRoot().resolve(getPath(first, more));
    }

    protected FileSystemProvider getProvider() {
        return fsRule.getTestFilesRoot().getFileSystem().provider();
    }

    protected FileSystemTestRule getFileSystemRule() {
        return fsRule;
    }

    private static final int MAX_ATTEMPTS = 50;

    public static Path generateUnusedPath(Path path) {
        Path candidate = path;

        int suffix = 1;
        while (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
            candidate = path.getFileSystem().getPath(path.toString() + suffix++);
            if (suffix > MAX_ATTEMPTS) {
                throw new IllegalStateException("Could not find unique version of " + path
                        + " after " + suffix + " attempts");
            }
        }

        return candidate;
    }

    public static String msg(Path path, String message) {
        return path.toAbsolutePath() + " " + message;
    }

}
