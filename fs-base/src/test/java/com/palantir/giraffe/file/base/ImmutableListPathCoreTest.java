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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

/**
 * Tests basic functionality of {@link ImmutableListPathCore}.
 *
 * @author bkeyes
 */
public class ImmutableListPathCoreTest {

    private static final String ROOT_NAME = "C:";

    private final ImmutableListPathCore empty = core(null);

    private final ImmutableListPathCore root = core("");
    private final ImmutableListPathCore rootNamed = core(ROOT_NAME);

    private final ImmutableListPathCore absolute = core("", "path", "to", "file");
    private final ImmutableListPathCore absoluteNamed = core(ROOT_NAME, "path", "to", "file");
    private final ImmutableListPathCore relative = core(null, "path", "to", "file");

    @Test
    public void absolutePathIsAbsolute() {
        assertTrue("absolute path is not absolute", absolute.isAbsolute());
        assertTrue("named absolute path is not absolute", absoluteNamed.isAbsolute());
    }

    @Test
    public void relativePathIsNotAbsolute() {
        assertFalse("relative path is absolute", relative.isAbsolute());
    }

    @Test
    public void rootIsAbsolute() {
        assertTrue("root is not absolute", root.isAbsolute());
        assertTrue("named root is not absolute", rootNamed.isAbsolute());
    }

    @Test
    public void getAbsoluteRoot() {
        ImmutableListPathCore absRoot = absolute.getRoot();
        ImmutableListPathCore namedAbsRoot = absoluteNamed.getRoot();

        assertThat("non-empty root path", absRoot.getPathSegments(), empty());
        assertEquals("wrong root name", "", absRoot.getRootString());

        assertThat("non-empty named root path", namedAbsRoot.getPathSegments(), empty());
        assertEquals("wrong root name", ROOT_NAME, namedAbsRoot.getRootString());
    }

    @Test
    public void relativeRootIsNull() {
        assertNull("relative root is not null", relative.getRoot());
    }

    @Test
    public void rootRootIsEqualToRoot() {
        assertEquals("unnamed root's root is not itself", root, root.getRoot());
        assertEquals("named root's root is not itself", rootNamed, rootNamed.getRoot());
    }

    @Test
    public void getAbsoluteFileName() {
        ImmutableListPathCore fileName = absolute.getFileName();
        assertFalse("file name path is absolute", fileName.isAbsolute());
        assertThat(fileName, hasPath("file"));
    }

    @Test
    public void getRelativeFileName() {
        ImmutableListPathCore fileName = relative.getFileName();
        assertFalse("file name path is absolute", fileName.isAbsolute());
        assertThat(fileName, hasPath("file"));
    }

    @Test
    public void rootFileNameIsNull() {
        assertNull("root file name is non-null", root.getFileName());
    }

    @Test
    public void getAbsoluteParent() {
        ImmutableListPathCore parent = absolute.getParent();
        ImmutableListPathCore namedParent = absoluteNamed.getParent();

        assertTrue("parent is not absolute", parent.isAbsolute());
        assertEquals("root is incorrect", "", parent.getRootString());
        assertThat(parent, hasPath("path", "to"));

        assertTrue("named parent is not absolute", namedParent.isAbsolute());
        assertEquals("named root is incorrect", ROOT_NAME, namedParent.getRootString());
        assertThat(namedParent, hasPath("path", "to"));
    }

    @Test
    public void getRelativeParent() {
        ImmutableListPathCore parent = relative.getParent();
        assertFalse("parent is absolute", parent.isAbsolute());
        assertThat(parent, hasPath("path", "to"));
    }

    @Test
    public void fileParentIsNull() {
        ImmutableListPathCore parent = core(null, "file").getParent();
        assertNull("file parent is not null", parent);
    }

    @Test
    public void rootFileParentIsRoot() {
        ImmutableListPathCore parent = core("", "file").getParent();
        assertEquals("parent is not root", root, parent);
    }

    @Test
    public void rootParentIsNull() {
        assertNull("root's parent is not null", root.getParent());
        assertNull("named root's parent is not null", rootNamed.getParent());
    }

    @Test
    public void rootNameCountIsZero() {
        assertEquals("root name count is non-zero", 0, root.getNameCount());
    }

    @Test
    public void getNameCount() {
        assertEquals("absolute name count is wrong", 3, absolute.getNameCount());
        assertEquals("relative name count is wrong", 3, relative.getNameCount());
    }

    @Test
    public void invalidNameIndexThrowsException() {
        try {
            absolute.getName(10);
            fail("name index > length allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }

        try {
            absolute.getName(-1);
            fail("name index < 0 allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void getName() {
        ImmutableListPathCore name = absolute.getName(1);
        assertFalse("name is absolute", name.isAbsolute());
        assertThat(name, hasPath("to"));
    }

    @Test
    public void invalidSubpathIndicesThrowsException() {
        try {
            absolute.subpath(-1, 2);
            fail("negative begin index allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }

        try {
            absolute.subpath(3, 4);
            fail("begin index == length allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }

        try {
            absolute.subpath(2, 0);
            fail("end index < begin index allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }

        try {
            absolute.subpath(0, 4);
            fail("end index > length allowed");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void fullSubpathIsEqualToPath() {
        ImmutableListPathCore sub = relative.subpath(0, 3);
        assertEquals("subpath is not equal to path", relative, sub);
    }

    @Test
    public void subpathIsRelative() {
        ImmutableListPathCore sub = absolute.subpath(0, 3);
        assertFalse("subpath is not relative", sub.isAbsolute());
    }

    @Test
    public void subpath() {
        ImmutableListPathCore sub = relative.subpath(1, 3);
        assertThat(sub, hasPath("to", "file"));
    }

    @Test
    public void pathStartsWithItself() {
        assertThat(relative, startsWith(relative));
        assertThat(absolute, startsWith(absolute));
    }

    @Test
    public void absolutePathStartsWithRoot() {
        assertThat(absolute, startsWith(root));
    }

    @Test
    public void absolutePathDoesNotStartWithDifferentRoot() {
        assertThat(absolute, notStartsWith(rootNamed));
    }

    @Test
    public void absoluteStartsWithAbsolute() {
        ImmutableListPathCore test = core("", "path", "to");
        assertThat(absolute, startsWith(test));
    }

    @Test
    public void absoluteDoesNotStartWithRelative() {
        assertThat(absolute, notStartsWith(relative));
    }

    @Test
    public void relativeStartsWithRelative() {
        ImmutableListPathCore test = core(null, "path", "to");
        assertThat(relative, startsWith(test));
    }

    @Test
    public void pathDoesNotStartWithLongerPath() {
        ImmutableListPathCore test = core("", "path", "to", "file", "one");
        assertThat(absolute, notStartsWith(test));
    }

    @Test
    public void pathEndsWithItself() {
        assertThat(relative, endsWith(relative));
        assertThat(absolute, endsWith(absolute));
    }

    @Test
    public void absoluteDoesNotEndWithAbsoluteWithDifferentRoot() {
        assertThat(absolute, notEndsWith(absoluteNamed));
    }

    @Test
    public void relativeDoesNotEndWithAbsolute() {
        assertThat(relative, notEndsWith(absolute));
    }

    @Test
    public void relativeEndsWithRelative() {
        ImmutableListPathCore test = core(null, "to", "file");
        assertThat(relative, endsWith(test));
    }

    @Test
    public void absoluteEndsWithRelative() {
        ImmutableListPathCore test = core(null, "to", "file");
        assertThat(absolute, endsWith(test));
    }

    @Test
    public void pathDoesNotEndWithLongerPath() {
        ImmutableListPathCore test = core(null, "path", "to", "file", "one");
        assertThat(absolute, notEndsWith(test));
    }

    @Test
    public void normalizeNormalPathIsIdentity() {
        ImmutableListPathCore normal = relative.normalize();
        assertThat("normalized normal path is not identity", normal, sameInstance(relative));
    }

    @Test
    public void normalizeCollapsesParents() {
        ImmutableListPathCore input = core(null, "a", "b", "..", "c", "d", "..", "..", "e");
        assertThat(input.normalize(), hasPath("a", "e"));
    }

    @Test
    public void normalizeCollapsesCurrent() {
        ImmutableListPathCore input = core(null, ".", "a", "b", ".", "c", "d", ".", ".", "e");
        assertThat(input.normalize(), hasPath("a", "b", "c", "d", "e"));
    }

    @Test
    public void normalizeCollapsesMixed() {
        ImmutableListPathCore input = core(null, "a", ".", "b", "..", "c", "d", ".", "..", "e");
        assertThat(input.normalize(), hasPath("a", "c", "e"));
    }

    @Test
    public void normalizePreservesLeadingParents() {
        ImmutableListPathCore input = core(null, "..", "..", "c", "d", "..", "e");
        assertThat(input.normalize(), hasPath("..", "..", "c", "e"));
    }

    @Test
    public void normalizePreserversInnerParentsThatBecomeLeading() {
        ImmutableListPathCore input = core(null, "a", "b", "..", "..", "d", "..", "..", "e");
        assertThat(input.normalize(), hasPath("..", "e"));
    }

    @Test
    public void normalizeRootParentIsRoot() {
        ImmutableListPathCore normal = core("", "..", "..", "c", "d", "..", "e").normalize();
        assertTrue("normalized absolute path is not absolute", normal.isAbsolute());
        assertThat(normal, hasPath("c", "e"));
    }

    @Test
    public void resolveAbsoluteIsPassthrough() {
        ImmutableListPathCore resolved = root.resolve(absolute);
        assertThat("resolved absolute is not input", resolved, sameInstance(absolute));
    }

    @Test
    public void resolveEmptyIsIdentity() {
        ImmutableListPathCore resolved = absolute.resolve(empty);
        assertThat("resolved empty is not identity", resolved, sameInstance(absolute));
    }

    @Test
    public void resolveOnAbsolute() {
        ImmutableListPathCore resolved = absolute.resolve(relative);
        assertTrue("resolved on absolute is not absolute", resolved.isAbsolute());
        assertThat(resolved, hasPath("path", "to", "file", "path", "to", "file"));
    }

    @Test
    public void resolveOnRelative() {
        ImmutableListPathCore resolved = relative.resolve(relative);
        assertFalse("resolved on relative is not relative", resolved.isAbsolute());
        assertThat(resolved, hasPath("path", "to", "file", "path", "to", "file"));
    }

    @Test
    public void resolveSiblingOnNoParentIsPassthrough() {
        ImmutableListPathCore resolved = root.resolveSibling(relative);
        assertThat("resolved sibling on root is not input", resolved, sameInstance(relative));
    }

    @Test
    public void resolveSiblingOnAbsolute() {
        ImmutableListPathCore resolved = absolute.resolveSibling(relative);
        assertTrue("resolved sibling on absolute is not absolute", resolved.isAbsolute());
        assertThat(resolved, hasPath("path", "to", "path", "to", "file"));
    }

    @Test
    public void resolveSiblingOnRelative() {
        ImmutableListPathCore resolved = relative.resolveSibling(relative);
        assertFalse("resolved sibling on relative is not relative", resolved.isAbsolute());
        assertThat(resolved, hasPath("path", "to", "path", "to", "file"));
    }

    @Test
    public void relativizeMixedPathsThrowsException() {
        try {
            absolute.relativize(relative);
            fail("relativized between absolute and relative allowed");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            relative.relativize(absolute);
            fail("relativized between relative and absolute allowed");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void relativizeParentsOnly() {
        ImmutableListPathCore target = core(null, "path");
        ImmutableListPathCore relativized = relative.relativize(target);
        assertFalse("relativized path is not relative", relativized.isAbsolute());
        assertThat(relativized, hasPath("..", ".."));
    }

    @Test
    public void relativizeDescentOnly() {
        ImmutableListPathCore target = core(null, "path", "to", "file", "or", "directory");
        ImmutableListPathCore relativized = relative.relativize(target);
        assertFalse("relativized path is not relative", relativized.isAbsolute());
        assertThat(relativized, hasPath("or", "directory"));
    }

    @Test
    public void relativizeParentsAndDescent() {
        ImmutableListPathCore target = core(null, "path", "to", "directory");
        ImmutableListPathCore relativized = relative.relativize(target);
        assertFalse("relativized path is not relative", relativized.isAbsolute());
        assertThat(relativized, hasPath("..", "directory"));
    }

    @Test
    public void relativizeAbsolutePaths() {
        ImmutableListPathCore target = core("", "path", "to", "directory");
        ImmutableListPathCore relativized = absolute.relativize(target);
        assertFalse("relativized path is not relative", relativized.isAbsolute());
        assertThat(relativized, hasPath("..", "directory"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void relativizeAbsolutePathsWithDifferentRootsThrowsException() {
        ImmutableListPathCore target = core(ROOT_NAME, "path", "to", "directory");
        absolute.relativize(target);
    }

    @Test
    public void relativizeSelfIsEmpty() {
        ImmutableListPathCore relativized = relative.relativize(relative);
        assertEquals("relativized path is not empty", empty, relativized);
    }

    @Test
    public void relativizeSelfAfterNormalizationIsEmpty() {
        ImmutableListPathCore target = core(null, "path", "to", ".", "file");
        ImmutableListPathCore relativized = relative.relativize(target);
        assertEquals("relativized path is not empty", empty, relativized);
    }

    @Test
    public void rootToPathString() {
        assertEquals("path string is wrong", "/", root.toPathString("/"));
        assertEquals("path string is wrong", "C:/", rootNamed.toPathString("/"));
    }

    @Test
    public void absoluteToPathString() {
        assertEquals("path string is wrong", "/path/to/file", absolute.toPathString("/"));
        assertEquals("path string is wrong", "C:/path/to/file", absoluteNamed.toPathString("/"));
    }

    @Test
    public void relativeToPathString() {
        assertEquals("path string is wrong", "path/to/file", relative.toPathString("/"));
    }

    // TODO(bkeyes): improve this matcher
    private static Matcher<ImmutableListPathCore> hasPath(String... path) {
        return new FeatureMatcher<ImmutableListPathCore, Iterable<String>>(
                contains(path), "path segment", "") {
            @Override
            protected Iterable<String> featureValueOf(ImmutableListPathCore actual) {
                return actual.getPathSegments();
            }
        };
    }

    private static Matcher<ImmutableListPathCore> startsWith(final ImmutableListPathCore test) {
        return new TypeSafeDiagnosingMatcher<ImmutableListPathCore>() {
            @Override
            public void describeTo(Description description) {
                String pathString = test.toPathString("/");
                description.appendText("startsWith(").appendValue(pathString).appendText(")");
            }

            @Override
            protected boolean matchesSafely(ImmutableListPathCore item, Description mismatch) {
                mismatch.appendText("false for path ").appendValue(item.toPathString("/"));
                return item.startsWith(test);
            }
        };
    }

    private static Matcher<ImmutableListPathCore> notStartsWith(final ImmutableListPathCore test) {
        return new TypeSafeDiagnosingMatcher<ImmutableListPathCore>() {
            @Override
            public void describeTo(Description description) {
                String pathString = test.toPathString("/");
                description.appendText("not startsWith(").appendValue(pathString).appendText(")");
            }

            @Override
            protected boolean matchesSafely(ImmutableListPathCore item, Description mismatch) {
                mismatch.appendText("true for path ").appendValue(item.toPathString("/"));
                return !item.startsWith(test);
            }
        };
    }

    private static Matcher<ImmutableListPathCore> endsWith(final ImmutableListPathCore test) {
        return new TypeSafeDiagnosingMatcher<ImmutableListPathCore>() {
            @Override
            public void describeTo(Description description) {
                String pathString = test.toPathString("/");
                description.appendText("endsWith(").appendValue(pathString).appendText(")");
            }

            @Override
            protected boolean matchesSafely(ImmutableListPathCore item, Description mismatch) {
                mismatch.appendText("false for path ").appendValue(item.toPathString("/"));
                return item.endsWith(test);
            }
        };
    }

    private static Matcher<ImmutableListPathCore> notEndsWith(final ImmutableListPathCore test) {
        return new TypeSafeDiagnosingMatcher<ImmutableListPathCore>() {
            @Override
            public void describeTo(Description description) {
                String pathString = test.toPathString("/");
                description.appendText("not endsWith(").appendValue(pathString).appendText(")");
            }

            @Override
            protected boolean matchesSafely(ImmutableListPathCore item, Description mismatch) {
                mismatch.appendText("true for path ").appendValue(item.toPathString("/"));
                return !item.endsWith(test);
            }
        };
    }

    private static ImmutableListPathCore core(String root, String... path) {
        return new ImmutableListPathCore(root, Arrays.asList(path));
    }
}
