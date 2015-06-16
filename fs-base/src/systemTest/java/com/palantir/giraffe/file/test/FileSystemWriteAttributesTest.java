package com.palantir.giraffe.file.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import com.palantir.giraffe.file.test.creator.AttributeTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of attribute modification.
 *
 * @author bkeyes
 */
public class FileSystemWriteAttributesTest extends FileSystemBaseTest {

    public FileSystemWriteAttributesTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test
    public void basicViewSetsModificationTime() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RW_MODIFIED_TIME);
        BasicFileAttributeView view = getView(path, BasicFileAttributeView.class);
        assertNotNull("BasicFileAttributeView is not available", view);

        FileTime oldTime = view.readAttributes().lastModifiedTime();
        FileTime newTime = FileTime.fromMillis(oldTime.toMillis() - 10000);
        view.setTimes(newTime, null, null);

        FileTime actualTime = view.readAttributes().lastModifiedTime();
        assertEquals("incorrect modification time", newTime, actualTime);
    }

    @Test
    public void posixViewSetsPermissions() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RW_PERMISSIONS);
        PosixFileAttributeView view = getView(path, PosixFileAttributeView.class);
        assertNotNull("PosixFileAttributeView is not available", view);

        Set<PosixFilePermission> oldPermissions = view.readAttributes().permissions();
        Set<PosixFilePermission> newPermissions = EnumSet.copyOf(oldPermissions);
        if (!newPermissions.add(PosixFilePermission.OWNER_EXECUTE)) {
            newPermissions.remove(PosixFilePermission.OWNER_EXECUTE);
        }
        view.setPermissions(newPermissions);

        Set<PosixFilePermission> actualPermissions = view.readAttributes().permissions();
        assertEquals("incorrect permissions", newPermissions, actualPermissions);
    }

    @Test
    public void dynamicSetLastModifiedTime() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RW_MODIFIED_TIME);

        FileTime oldTime = (FileTime) Files.getAttribute(path, "lastModifiedTime");
        FileTime newTime = FileTime.fromMillis(oldTime.toMillis() - 10000);
        getProvider().setAttribute(path, "lastModifiedTime", newTime);

        FileTime actualTime = (FileTime) Files.getAttribute(path, "lastModifiedTime");
        assertEquals("incorrect modification time", newTime, actualTime);
    }

    @Test
    public void dynamicSetPermissions() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RW_PERMISSIONS);

        @SuppressWarnings("unchecked")
        Set<PosixFilePermission> oldPermissions = (Set<PosixFilePermission>) Files.getAttribute(
                path, "posix:permissions");

        Set<PosixFilePermission> newPermissions = EnumSet.copyOf(oldPermissions);
        if (!newPermissions.add(PosixFilePermission.OWNER_EXECUTE)) {
            newPermissions.remove(PosixFilePermission.OWNER_EXECUTE);
        }
        getProvider().setAttribute(path, "posix:permissions", newPermissions);

        @SuppressWarnings("unchecked")
        Set<PosixFilePermission> actualPermissions = (Set<PosixFilePermission>) Files.getAttribute(
                path, "posix:permissions");

        assertEquals("incorrect permissions", newPermissions, actualPermissions);
    }

    private <V extends FileAttributeView> V getView(Path path, Class<V> type) {
        return getProvider().getFileAttributeView(path, type);
    }
}
