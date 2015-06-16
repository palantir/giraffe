package com.palantir.giraffe.file.test;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import com.palantir.giraffe.file.test.creator.AccessTestCreator;
import com.palantir.giraffe.file.test.creator.SymlinkTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of access and existence checks.
 *
 * @author bkeyes
 */
public class FileSystemAccessTest extends FileSystemBaseTest {

    public FileSystemAccessTest(FileSystemTestRule testSystem) {
        super(testSystem);
    }

    @Test
    public void fileExists() throws IOException {
        Path exists = getTestPath(AccessTestCreator.F_RO_EXISTS);
        try {
            getProvider().checkAccess(exists);
        } catch (NoSuchFileException e) {
            fail(exists, "does not exist");
        }

        Path none = getTestPath(AccessTestCreator.F_RO_NONE);
        try {
            getProvider().checkAccess(none);
        } catch (NoSuchFileException e) {
            fail(none, "does not exist");
        }
    }

    @Test
    public void fileNotExists() throws IOException {
        Path notExists = getTestPath("not_exists.txt");
        try {
            getProvider().checkAccess(notExists);
            fail(notExists, "exists");
        } catch (NoSuchFileException expected) {
            // expected
        }
    }

    @Test
    public void fileIsReadable() throws IOException {
        Path none = getTestPath(AccessTestCreator.F_RO_NONE);
        try {
            getProvider().checkAccess(none, AccessMode.READ);
            fail(none, "is readable");
        } catch (AccessDeniedException expected) {
            // expected
        }

        Path readable = getTestPath(AccessTestCreator.F_RO_READABLE);
        try {
            getProvider().checkAccess(readable, AccessMode.READ);
        } catch (AccessDeniedException e) {
            fail(readable, "is not readable");
        }
    }

    @Test
    public void fileIsWriteable() throws IOException {
        Path readable = getTestPath(AccessTestCreator.F_RO_READABLE);
        try {
            getProvider().checkAccess(readable, AccessMode.WRITE);
            fail(readable, "is writable");
        } catch (AccessDeniedException expected) {
            // expected
        }

        Path writeable = getTestPath(AccessTestCreator.F_RO_WRITEABLE);
        try {
            getProvider().checkAccess(writeable, AccessMode.WRITE);
        } catch (AccessDeniedException e) {
            fail(writeable, "is not writable");
        }
    }

    @Test
    public void fileIsExecutable() throws IOException {
        Path readable = getTestPath(AccessTestCreator.F_RO_READABLE);
        try {
            getProvider().checkAccess(readable, AccessMode.EXECUTE);
            fail(readable, "is executable");
        } catch (AccessDeniedException expected) {
            // expected
        }

        Path executable = getTestPath(AccessTestCreator.F_RO_EXECUTABLE);
        try {
            getProvider().checkAccess(executable, AccessMode.EXECUTE);
        } catch (AccessDeniedException e) {
            fail(executable, "is not executable");
        }
    }

    @Test
    public void checkAccessFollowsSymbolicLinks() throws IOException {
        Path link = getTestPath(SymlinkTestCreator.F_RO_SYMLINK);
        try {
            getProvider().checkAccess(link);
        } catch (NoSuchFileException e) {
            fail(link, "does not exist");
        }

        Path brokenLink = getTestPath(SymlinkTestCreator.F_RO_BROKEN_SYMLINK);
        try {
            getProvider().checkAccess(brokenLink);
            fail(brokenLink, "exists");
        } catch (NoSuchFileException expected) {
            // expected
        }
    }

    private static void fail(Path path, String message) {
        Assert.fail(path.toAbsolutePath() + " " + message);
    }

}
