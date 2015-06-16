package com.palantir.giraffe.ssh.suite;

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
import com.palantir.giraffe.ssh.util.RemoteSshFileSystemRule;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the SSH file system implementation against Linux.
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
    FileSystemProviderMismatchTest.class
})
public class LinuxFileSystemSuite implements Filterable {

    @ClassRule
    public static final FileSystemTestRule FS_RULE =
            new RemoteSshFileSystemRule("linux", "file");

    @Override
    public Filter getFilter() {
        return Filter.ALL;
    }
}
