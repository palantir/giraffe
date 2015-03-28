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
