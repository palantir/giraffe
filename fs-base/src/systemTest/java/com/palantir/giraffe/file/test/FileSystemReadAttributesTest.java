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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.junit.Test;

import com.palantir.giraffe.file.test.creator.AttributeTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of attribute access.
 *
 * @author bkeyes
 */
public class FileSystemReadAttributesTest extends FileSystemBaseTest {

    public FileSystemReadAttributesTest(FileSystemTestRule fsRule) {
        super(fsRule);
    }

    @Test
    public void basicViewReadsModificationTime() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_MODIFIED_TIME);
        BasicFileAttributeView view = getView(path, BasicFileAttributeView.class);
        assertNotNull("BasicFileAttributeView is not available", view);

        FileTime time = view.readAttributes().lastModifiedTime();
        assertEquals("incorrect modification time", AttributeTestCreator.MODIFIED_TIME, time);
    }

    @Test
    public void basicViewReadsSize() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_SIZE);
        BasicFileAttributeView view = getView(path, BasicFileAttributeView.class);
        assertNotNull("BasicFileAttributeView is not available", view);

        long size = view.readAttributes().size();
        assertEquals("incorrect size", AttributeTestCreator.SIZE, size);
    }

    @Test
    public void basicViewReadsEmptySize() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_EMPTY);
        BasicFileAttributeView view = getView(path, BasicFileAttributeView.class);
        assertNotNull("BasicFileAttributeView is not available", view);

        long size = view.readAttributes().size();
        assertEquals("incorrect size", 0, size);
    }

    @Test
    public void basicViewAgreesWithStaticRead() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_MODIFIED_TIME);
        BasicFileAttributeView view = getView(path, BasicFileAttributeView.class);
        assertNotNull("BasicFileAttributeView is not available", view);

        FileTime viewTime = view.readAttributes().lastModifiedTime();
        FileTime readTime = readAttributes(path, BasicFileAttributes.class).lastModifiedTime();

        assertTrue(String.format("view time [%s] != read time [%s]", viewTime, readTime),
                viewTime.equals(readTime));
    }

    @Test
    public void posixViewReadsPermissions() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_PERMISSIONS);
        PosixFileAttributeView view = getView(path, PosixFileAttributeView.class);
        assertNotNull("PosixFileAttributeView is not available", view);

        Set<PosixFilePermission> permissions = view.readAttributes().permissions();
        assertEquals("incorrect permissions", AttributeTestCreator.PERMISSIONS, permissions);
    }

    @Test
    public void posixViewAgreesWithStaticRead() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_PERMISSIONS);
        PosixFileAttributeView view = getView(path, PosixFileAttributeView.class);
        assertNotNull("PosixFileAttributeView is not available", view);

        Set<PosixFilePermission> viewPerms = view.readAttributes().permissions();
        Set<PosixFilePermission> readPerms = readAttributes(path,
                PosixFileAttributes.class).permissions();

        assertTrue(String.format(
                "view permissions [%s] != read permissions [%s]", viewPerms, readPerms),
                viewPerms.equals(readPerms));
    }

    @Test
    public void dynamicReadsModifiedTime() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_MODIFIED_TIME);

        FileTime time = (FileTime) Files.getAttribute(path, "lastModifiedTime");
        assertEquals("incorrect modification time", AttributeTestCreator.MODIFIED_TIME, time);
    }

    @Test
    public void dynamicReadsPermissions() throws IOException {
        Path path = getTestPath(AttributeTestCreator.F_RO_PERMISSIONS);

        @SuppressWarnings("unchecked")
        Set<PosixFilePermission> permissions = (Set<PosixFilePermission>)
                Files.getAttribute(path, "posix:permissions");
        assertEquals("incorrect permissions", AttributeTestCreator.PERMISSIONS, permissions);
    }

    private <V extends FileAttributeView> V getView(Path path, Class<V> type) {
        return getProvider().getFileAttributeView(path, type);
    }

    private <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type)
            throws IOException {
        return getProvider().readAttributes(path, type);
    }
}
