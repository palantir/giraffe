package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;

/**
 * Utilities for creating standard {@link PathMatcher}s.
 *
 * @author bkeyes
 */
public final class StandardPathMatchers {

    private static final String GLOB_SYNTAX = "glob";
    private static final String REGEX_SYNTAX = "regex";

    public static PathMatcher fromSyntaxAndPattern(String syntaxAndPattern, String separator) {
        checkNotNull(syntaxAndPattern, "syntaxAndPattern must be non-null");

        int split = syntaxAndPattern.indexOf(':');
        if (split <= 0 || split == syntaxAndPattern.length()) {
            throw new IllegalArgumentException("input must have form 'syntax:pattern'");
        }

        String syntax = syntaxAndPattern.substring(0, split);
        String pattern = syntaxAndPattern.substring(split + 1);
        switch (syntax) {
            case GLOB_SYNTAX:
                return fromRegex(Globs.toRegex(pattern, separator));
            case REGEX_SYNTAX:
                return fromRegex(pattern);
            default:
                throw new UnsupportedOperationException("syntax '" + syntax + "' not supported");
        }
    }

    public static PathMatcher fromRegex(String regex) {
        return new RegexMatcher(Pattern.compile(regex));
    }

    public static PathMatcher fromRegex(Pattern pattern) {
        return new RegexMatcher(pattern);
    }

    private static final class RegexMatcher implements PathMatcher {
        private final Pattern pattern;

        private RegexMatcher(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Path path) {
            return pattern.matcher(path.toString()).matches();
        }
    }

    private StandardPathMatchers() {
        throw new UnsupportedOperationException();
    }
}
