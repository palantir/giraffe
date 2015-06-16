package com.palantir.giraffe.file.test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.palantir.giraffe.file.test.creator.DirectoryTestCreator;
import com.palantir.giraffe.file.test.runner.FileSystemTestRule;

/**
 * Tests a file system's implementation of directory listing.
 *
 * @author bkeyes
 */
public final class FileSystemListDirectoryTest extends FileSystemBaseTest {

    public FileSystemListDirectoryTest(FileSystemTestRule testSystem) {
        super(testSystem);
    }

    @Test
    public void listsDirectory() throws IOException {
        Path dir = getTestPath(DirectoryTestCreator.F_RO_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            assertStreamEquals(stream, dir,
                    DirectoryTestCreator.F_RO_LOG,
                    DirectoryTestCreator.F_RO_SUBDIR,
                    DirectoryTestCreator.F_RO_SYMLINK,
                    DirectoryTestCreator.F_RO_TEXT);
        }
    }

    @Test
    public void appliesFilter() throws IOException {
        Filter<Path> filter = new Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS);
            }
        };

        Path dir = getTestPath(DirectoryTestCreator.F_RO_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            assertStreamEquals(stream, dir,
                    DirectoryTestCreator.F_RO_LOG,
                    DirectoryTestCreator.F_RO_TEXT);
        }
    }

    @Test
    public void appliesTextFilter() throws IOException {
        Path dir = getTestPath(DirectoryTestCreator.F_RO_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
            assertStreamEquals(stream, dir,
                    DirectoryTestCreator.F_RO_SYMLINK,
                    DirectoryTestCreator.F_RO_TEXT);
        }
    }

    private static void assertStreamEquals(DirectoryStream<Path> stream, Path dir,
            String... children) {
        Collection<Matcher<? super Path>> childMatchers = new HashSet<>();
        for (String child : children) {
            childMatchers.add(equalTo(dir.resolve(child)));
        }

        List<Path> paths = new ArrayList<>();
        for (Path p : stream) {
            paths.add(p);
        }

        assertThat("incorrect children", paths, containsInAnyOrder(childMatchers));
    }
}
