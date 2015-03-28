package com.palantir.giraffe.file.test;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests that methods on file system providers reject incompatible paths.
 *
 * @author bkeyes
 */
public class FileSystemProviderMismatchTest extends FileSystemBaseTest {

    public FileSystemProviderMismatchTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test(expected = ProviderMismatchException.class)
    public void rejectsIncompatiblePaths() throws IOException {
        getProvider().checkAccess(getFileSystemRule().getIncompatiblePath());
    }

    @Test(expected = ProviderMismatchException.class)
    public void deleteRejectsIncompatiblePath() throws IOException {
        getProvider().delete(getFileSystemRule().getIncompatiblePath());
    }

    @Test(expected = ProviderMismatchException.class)
    public void deleteIfExistsRejectsIncompatiblePath() throws IOException {
        getProvider().deleteIfExists(getFileSystemRule().getIncompatiblePath());
    }

    @Test(expected = ProviderMismatchException.class)
    public void createRejectsIncompatiblePath() throws IOException {
        getProvider().createDirectory(getFileSystemRule().getIncompatiblePath());
    }

    @Test(expected = ProviderMismatchException.class)
    public void newByteChannelRejectsIncompatiblePath() throws IOException {
        Set<OpenOption> options = Collections.emptySet();
        getProvider().newByteChannel(getFileSystemRule().getIncompatiblePath(), options);
    }

    @Test(expected = ProviderMismatchException.class)
    public void readStaticRejectsIncompatiblePaths() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().readAttributes(path, BasicFileAttributes.class);
    }

    @Test(expected = ProviderMismatchException.class)
    public void readDynamicRejectsIncompatiblePaths() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().readAttributes(path, "*");
    }

    @Test(expected = ProviderMismatchException.class)
    public void moveRejectsIncompatibleSourcePath() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().move(path, getTestPath("target"));
    }

    @Test(expected = ProviderMismatchException.class)
    public void moveRejectsIncompatibleTargetPath() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().move(getTestPath("target"), path);
    }

    @Test(expected = ProviderMismatchException.class)
    public void copyRejectsIncompatibleSourcePath() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().copy(path, getTestPath("target"));
    }

    @Test(expected = ProviderMismatchException.class)
    public void copyRejectsIncompatibleTargetPath() throws IOException {
        Path path = getFileSystemRule().getIncompatiblePath();
        getProvider().copy(getTestPath("target"), path);
    }

}
