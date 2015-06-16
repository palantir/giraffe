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
package com.palantir.giraffe.file.base;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.palantir.giraffe.file.base.ImmutableListPathCore.Parser;
import com.palantir.giraffe.file.base.ImmutableListPathCore.Parser.NamedRootFormat;

/**
 * Tests basic functionality of {@link ImmutableListPathCore.Parser}.
 *
 * @author bkeyes
 */
public class ImmutableListPathCoreParserTest {

    private static final Parser UNIX_PARSER = ImmutableListPathCore.parser("/");

    private static final Parser WINDOWS_PARSER = ImmutableListPathCore.namedRootParser(
            "\\", NamedRootFormat.POSTFIX, ":");

    private static final Parser UNIFORM_PARSER = ImmutableListPathCore.namedRootParser(
            "/", NamedRootFormat.OPTIONAL_PREFIX, "//");

    @Test
    public void unixRelative() {
        ImmutableListPathCore path1 = UNIX_PARSER.parse("path");
        assertFalse("path is not relative", path1.isAbsolute());
        assertThat(path1, hasPath("path"));

        ImmutableListPathCore path2 = UNIX_PARSER.parse("path/to/file");
        assertFalse("path is not relative", path2.isAbsolute());
        assertThat(path2, hasPath("path", "to", "file"));
    }

    @Test
    public void windowsRelative() {
        ImmutableListPathCore path1 = WINDOWS_PARSER.parse("path");
        assertFalse("path is not relative", path1.isAbsolute());
        assertThat(path1, hasPath("path"));

        ImmutableListPathCore path2 = WINDOWS_PARSER.parse("path\\to\\file");
        assertFalse("path is not relative", path2.isAbsolute());
        assertThat(path2, hasPath("path", "to", "file"));
    }

    @Test
    public void uniformRelative() {
        ImmutableListPathCore path1 = UNIFORM_PARSER.parse("path");
        assertFalse("path is not relative", path1.isAbsolute());
        assertThat(path1, hasPath("path"));

        ImmutableListPathCore path2 = UNIFORM_PARSER.parse("path/to/file");
        assertFalse("path is not relative", path2.isAbsolute());
        assertThat(path2, hasPath("path", "to", "file"));
    }

    @Test
    public void unixAbsolute() {
        ImmutableListPathCore path = UNIX_PARSER.parse("/path/to/file");
        assertTrue("path is not absolute", path.isAbsolute());
        assertThat(path, hasPath("path", "to", "file"));
    }

    @Test
    public void windowsAbsolute() {
        ImmutableListPathCore path = WINDOWS_PARSER.parse("C:\\path\\to\\file");
        assertTrue("path is not absolute", path.isAbsolute());
        assertEquals("incorrect root", "C:", path.getRootString());
        assertThat(path, hasPath("path", "to", "file"));
    }

    @Test
    public void uniformAbsolute() {
        ImmutableListPathCore path = UNIFORM_PARSER.parse("/path/to/file");
        assertTrue("path is not absolute", path.isAbsolute());
        assertThat(path, hasPath("path", "to", "file"));
    }

    @Test
    public void uniformNamedRootAbsolute() {
        ImmutableListPathCore path = UNIFORM_PARSER.parse("//root/path/to/file");
        assertTrue("path is not absolute", path.isAbsolute());
        assertEquals("incorrect root", "//root", path.getRootString());
        assertThat(path, hasPath("path", "to", "file"));
    }

    @Test
    public void removesEmptySegments() {
        ImmutableListPathCore path = UNIX_PARSER.parse("this//path/has/////empty/segments/");
        assertThat(path, hasPath("this", "path", "has", "empty", "segments"));
    }

    // TODO(bkeyes): duplicated from ImmutableListPathCoreTest
    private static Matcher<ImmutableListPathCore> hasPath(String... path) {
        return new FeatureMatcher<ImmutableListPathCore, Iterable<String>>(
                contains(path), "path segment", "") {
            @Override
            protected Iterable<String> featureValueOf(ImmutableListPathCore actual) {
                return actual.getPathSegments();
            }
        };
    }

}
