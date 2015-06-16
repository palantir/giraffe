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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

import java.nio.file.InvalidPathException;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.palantir.giraffe.file.base.ImmutableListPathCore.Parser.NamedRootFormat;

/**
 * A {@link PathCore} implementation backed by an {@link ImmutableList} of
 * segments.
 *
 * @author bkeyes
 */
public final class ImmutableListPathCore implements PathCore<ImmutableListPathCore> {

    private static final String CURRENT_DIR = ".";
    private static final String PARENT_DIR = "..";

    private static final ImmutableListPathCore emptyInstance =
            new ImmutableListPathCore(null, ImmutableList.<String>of());

    /**
     * Constructs a new parser for path strings with a single root. Each segment
     * of the path is separated by the given string. Absolute paths start with
     * the given string.
     *
     * @param separator the path separator
     *
     * @return a new parser
     */
    public static Parser parser(String separator) {
        return new Parser(separator, null, null);
    }

    /**
     * Constructs a new parser for path strings with named roots. Each segment
     * of the path is separated by the given separator string.
     * <p>
     * Roots are parsed based on the root format and the root identifier string.
     * For {@linkplain NamedRootFormat#PREFIX prefix} roots, absolute paths
     * start with the identifier string. The root name consists of all
     * characters from the start of the string to the first path separator,
     * exclusive. For {@linkplain NamedRootFormat#POSTFIX postfix} roots,
     * absolute paths contain the identifier string. The root name consists of
     * all characters from the start of the string to the identifier, inclusive.
     * The path separator must immediately follow the identifier.
     * <p>
     * For {@linkplain NamedRootFormat#OPTIONAL_PREFIX optional prefix} roots,
     * absolute paths start with either the identifier string as described above
     * or the path separator. Absolute paths that start with the path separator
     * have an unnamed (empty) root.
     *
     * @param separator the path separator
     * @param format the root format
     * @param rootId the root identifier
     *
     * @return a new parser
     */
    public static Parser namedRootParser(String separator, NamedRootFormat format, String rootId) {
        checkNotNull(format, "format must be non-null");
        checkNotNull(rootId, "rootId must be non-null");
        checkArgument(!rootId.isEmpty(), "rootId must be non-empty for named roots");
        return new Parser(separator, format, rootId);
    }

    /**
     * Creates {@code ImmutableListPathCore} instances from path strings.
     *
     * @author bkeyes
     */
    public static final class Parser {

        /**
         * Specifies the format of named roots in a path string.
         */
        public enum NamedRootFormat {
            /**
             * All roots are identified by a prefix string.
             */
            PREFIX,

            /**
             * Named roots are identified by a prefix string while the unamed
             * root is identified by the path separator.
             */
            OPTIONAL_PREFIX,

            /**
             * All roots are identified by a postfix string.
             */
            POSTFIX;
        }

        private final String separator;
        private final NamedRootFormat rootFormat;
        private final String rootId;

        private Parser(String separator, NamedRootFormat rootFormat, String rootId) {
            this.separator = checkNotNull(separator, "separator must be non-null");
            checkArgument(!separator.isEmpty(), "separator must be non-empty");

            this.rootFormat = rootFormat;
            this.rootId = rootId;
        }

        public ImmutableListPathCore parse(String path) {
            checkCharacters(path);

            int pathStart = 0;
            String root = null;

            if (isRootPrefix(rootFormat) && path.startsWith(rootId)) {
                int end = path.indexOf(separator, rootId.length());
                if (end == rootId.length()) {
                    throw new InvalidPathException(path, "root name is empty", end);
                } else if (end < 0) {
                    throw new InvalidPathException(path, "root is not terminated", path.length());
                }
                root = path.substring(0, end);
                pathStart = end + separator.length();
            } else if (rootFormat == NamedRootFormat.POSTFIX) {
                int id = path.indexOf(rootId);
                if (id > 0) {
                    int rootEnd = id + rootId.length();
                    root = path.substring(0, rootEnd);
                    checkPostfixSeparator(path, rootEnd);
                    pathStart = rootEnd + separator.length();
                } else if (id == 0) {
                    throw new InvalidPathException(path, "root name is empty", id);
                }
            } else if (isRootSeparator(rootFormat) && path.startsWith(separator)) {
                root = "";
                pathStart = separator.length();
            }

            ImmutableList.Builder<String> validSegments = ImmutableList.builder();
            for (String segment : path.substring(pathStart).split(Pattern.quote(separator))) {
                if (!segment.isEmpty()) {
                    validSegments.add(segment);
                }
            }
            return new ImmutableListPathCore(root, validSegments.build());
        }

        private static void checkCharacters(String path) {
            int nullIndex = path.indexOf('\u0000');
            if (nullIndex >= 0) {
                throw new InvalidPathException(path, "NUL character not allowed", nullIndex);
            }
        }

        private static boolean isRootPrefix(NamedRootFormat type) {
            return type == NamedRootFormat.PREFIX || type == NamedRootFormat.OPTIONAL_PREFIX;
        }

        private static boolean isRootSeparator(NamedRootFormat type) {
            return type == null || type == NamedRootFormat.OPTIONAL_PREFIX;
        }

        private void checkPostfixSeparator(String path, int end) {
            if (!separator.equals(path.substring(end, end + separator.length()))) {
                throw new InvalidPathException(path, "root is not follwed by separator", end);
            }
        }
    }

    @Nullable
    private final String root;
    private final ImmutableList<String> path;

    /**
     * Constructs a new core instance.
     *
     * @param root the root component, or {@code null} if this path is relative.
     * @param path the segments that make up this path
     */
    public ImmutableListPathCore(@Nullable String root, Iterable<String> path) {
        this.root = root;
        this.path = ImmutableList.copyOf(checkNotNull(path, "path must be non-null"));
    }

    @Override
    public boolean isAbsolute() {
        return root != null;
    }

    @Override
    public ImmutableListPathCore getRoot() {
        if (isAbsolute()) {
            return new ImmutableListPathCore(root, ImmutableList.<String>of());
        } else {
            return null;
        }
    }

    @Override
    public ImmutableListPathCore getFileName() {
        if (getNameCount() == 0) {
            return null;
        } else {
            return getName(getNameCount() - 1);
        }
    }

    @Override
    public ImmutableListPathCore getParent() {
        if (getNameCount() > (isAbsolute() ?  0 : 1)) {
            return new ImmutableListPathCore(root, path.subList(0, getNameCount() - 1));
        } else {
            return null;
        }
    }

    @Override
    public int getNameCount() {
        return path.size();
    }

    @Override
    public ImmutableListPathCore getName(int index) {
        checkElementIndex(index, getNameCount(), "index");
        return new ImmutableListPathCore(null, ImmutableList.of(path.get(index)));
    }

    @Override
    public ImmutableListPathCore subpath(int beginIndex, int endIndex) {
        checkPositionIndex(endIndex, getNameCount(), "endIndex");
        checkElementIndex(beginIndex, endIndex, "beginIndex");
        return new ImmutableListPathCore(null, path.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(ImmutableListPathCore other) {
        if (other.isAbsolute() != isAbsolute()) {
            return false;
        } else if (other.path.size() > path.size()) {
            return false;
        } else {
            boolean rootStartsWith = Objects.equals(root, other.root);
            return rootStartsWith && other.path.equals(path.subList(0, other.path.size()));
        }
    }

    @Override
    public boolean endsWith(ImmutableListPathCore other) {
        if (other.isAbsolute()) {
            return isAbsolute() && this.equals(other);
        } else if (other.path.size() > path.size()) {
            return false;
        } else {
            int start = path.size() - other.path.size();
            return other.path.equals(path.subList(start, path.size()));
        }
    }

    @Override
    public ImmutableListPathCore normalize() {
        boolean[] ignore = new boolean[path.size()];
        int ignoreCount = 0;

        // mark redundant elements as ignored
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).equals(CURRENT_DIR)) {
                ignore[i] = true;
                ignoreCount++;
            } else if (path.get(i).equals(PARENT_DIR)) {
                // search backwards to find first unignored element
                int j = i - 1;
                while (j >= 0 && ignore[j]) {
                    j--;
                }

                if (j >= 0 && !path.get(j).equals(PARENT_DIR)) {
                    // path matches "name/[ignored]/.."
                    // ignore both name and ".."
                    ignore[i] = true;
                    ignore[j] = true;
                    ignoreCount += 2;
                } else if (j < 0 && isAbsolute()) {
                    // path matches "/[ignored]/.."
                    // ignore ".." (parent of / is /)
                    ignore[i] = true;
                    ignoreCount++;
                }
            }
        }

        if (ignoreCount == 0) {
            return this;
        } else {
            ImmutableList.Builder<String> normalized = ImmutableList.builder();
            for (int i = 0; i < path.size(); i++) {
                if (!ignore[i]) {
                    normalized.add(path.get(i));
                }
            }
            return new ImmutableListPathCore(root, normalized.build());
        }
    }

    @Override
    public ImmutableListPathCore resolve(ImmutableListPathCore other) {
        if (other.isAbsolute()) {
            return other;
        } else if (other.getNameCount() == 0) {
            return this;
        } else {
            ImmutableList<String> combinedNames = ImmutableList.<String>builder()
                    .addAll(path)
                    .addAll(other.path)
                    .build();
            return new ImmutableListPathCore(root, combinedNames);
        }
    }

    @Override
    public ImmutableListPathCore resolveSibling(ImmutableListPathCore other) {
        ImmutableListPathCore parent = getParent();
        return (parent == null) ? other : parent.resolve(other);
    }

    @Override
    public ImmutableListPathCore relativize(ImmutableListPathCore other) {
        if (this.equals(other)) {
            return emptyInstance;
        } else if (isAbsolute() && !other.isAbsolute()) {
            throw new IllegalArgumentException(
                    "cannot relativize from absolute path to relative path");
        } else if (!isAbsolute() && other.isAbsolute()) {
            throw new IllegalArgumentException(
                    "cannot relativize from relative path to absolute path");
        } else if (isAbsolute() && !getRootString().equals(other.getRootString())) {
            throw new IllegalArgumentException(
                    "cannot relativize between paths with different roots");
        } else {
            return relativizeNormalizedPaths(normalize(), other.normalize());
        }
    }

    private static ImmutableListPathCore relativizeNormalizedPaths(ImmutableListPathCore base,
            ImmutableListPathCore other) {
        if (base.equals(other)) {
            return emptyInstance;
        } else {
            // find longest common prefix
            int i = 0;
            int limit = Math.min(base.path.size(), other.path.size());
            while (i < limit && base.path.get(i).equals(other.path.get(i))) {
                i++;
            }

            ImmutableList.Builder<String> relative = ImmutableList.builder();
            // go up once for each remaining name in this path
            relative.addAll(Collections.nCopies(Math.max(0, base.path.size() - i), PARENT_DIR));
            // append the remaining names from the other path
            relative.addAll(other.path.subList(i, other.path.size()));
            return new ImmutableListPathCore(null, relative.build());
        }
    }

    @Override
    public String toPathString(String separator) {
        checkNotNull(separator, "separator must be non-null");
        checkArgument(!separator.isEmpty(), "separator must be non-empty");

        StringBuilder pathString = new StringBuilder();
        if (isAbsolute()) {
            pathString.append(root).append(separator);
        }
        Joiner.on(separator).appendTo(pathString, path);
        return pathString.toString();
    }

    @Override
    public String toString() {
        return "ImmutableListPathCore[root=" + root + ", path=" + path + "]";
    }

    @Override
    public int compareTo(ImmutableListPathCore other) {
        return ComparisonChain.start()
                .compare(root, other.root, Ordering.natural().nullsLast())
                .compare(path, other.path, Ordering.<String>natural().lexicographical())
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ImmutableListPathCore)) {
            return false;
        } else {
            ImmutableListPathCore other = (ImmutableListPathCore) obj;
            return Objects.equals(root, other.root) && path.equals(other.path);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, path);
    }

    /**
     * Returns the root component of this path, or {@code null} if this path is
     * relative.
     */
    @Nullable
    public String getRootString() {
        return root;
    }

    public ImmutableList<String> getPathSegments() {
        return path;
    }
}
