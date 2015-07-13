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
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.PatternSyntaxException;

/**
 * Utility methods for working with
 * {@linkplain java.nio.file.FileSystem#getPathMatcher(String) glob} patterns.
 *
 * @author bkeyes
 */
public final class Globs {

    /**
     * Converts the given glob pattern into a regular expression.
     *
     * @param glob the glob pattern to convert
     * @param separator the path separator character
     *
     * @return a regular expression matching the same inputs as {@code glob}
     *
     * @throws PatternSyntaxException if the pattern is invalid
     * @throws IllegalArgumentException if {@code separator} has multiple
     *         characters
     */
    public static String toRegex(String glob, String separator) {
        checkNotNull(separator, "separator must be non-null");
        checkArgument(separator.length() == 1,
                "separator must be a single character: %s",
                separator);
        return toRegex(glob, separator.charAt(0));
    }

    /**
     * Converts the given glob pattern into a regular expression.
     *
     * @param glob the glob pattern to convert
     * @param separator the path separator character
     *
     * @return a regular expression matching the same inputs as {@code glob}
     *
     * @throws PatternSyntaxException if the pattern is invalid
     */
    public static String toRegex(String glob, char separator) {
        return new GlobToRegexParser(glob, separator).parseToRegex();
    }

    private Globs() {
        throw new UnsupportedOperationException();
    }
}
