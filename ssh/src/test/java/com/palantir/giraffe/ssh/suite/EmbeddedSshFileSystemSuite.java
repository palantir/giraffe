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
package com.palantir.giraffe.ssh.suite;

import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.Suite.SuiteClasses;

import com.palantir.giraffe.file.test.FileSystemAccessTest;
import com.palantir.giraffe.file.test.FileSystemCRUDTest;
import com.palantir.giraffe.file.test.FileSystemCopyMoveTest;
import com.palantir.giraffe.file.test.FileSystemListDirectoryTest;
import com.palantir.giraffe.file.test.FileSystemProviderMismatchTest;
import com.palantir.giraffe.file.test.FileSystemReadAttributesTest;
import com.palantir.giraffe.file.test.FileSystemSymlinkTest;
import com.palantir.giraffe.file.test.FileSystemWriteAttributesTest;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;
import com.palantir.giraffe.ssh.FileSystemUpgradeTest;
import com.palantir.giraffe.ssh.util.MinaSshdFileSystemRule;
import com.palantir.giraffe.test.runner.RemoveTestsFilter;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the SSH file system implementation.
 *
 * @author bkeyes
 */
@RunWith(SystemSuite.class)
@SystemRule(FileSystemTestRule.class)
@SuiteClasses({
    FileSystemAccessTest.class,
    FileSystemReadAttributesTest.class,
    FileSystemWriteAttributesTest.class,
    FileSystemListDirectoryTest.class,
    FileSystemCRUDTest.class,
    FileSystemSymlinkTest.class,
    FileSystemCopyMoveTest.class,
    FileSystemProviderMismatchTest.class,
    FileSystemUpgradeTest.class
})
public class EmbeddedSshFileSystemSuite implements Filterable {

    @ClassRule
    public static final FileSystemTestRule FS_RULE =
            new MinaSshdFileSystemRule(Paths.get("build/system-test-files/file"));

    @Override
    public Filter getFilter() {
        return new RemoveTestsFilter().remove(
                // MINA doesn't implement lstat() properly
                "com.palantir.giraffe.file.test.FileSystemSymlinkTest.brokenSymlinkExists",
                "com.palantir.giraffe.file.test.FileSystemSymlinkTest.deleteFilePreservesSymlink");
    }
}
