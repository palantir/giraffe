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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.PatternSyntaxException;

final class GlobToRegexParser {

    private static final String REGEX_META_CHARS = "\\.*?+^$()[]{}|";
    private static final String GLOB_META_CHARS = "\\*?[]{},";

    private final String glob;
    private final char separator;

    private int index = 0;
    private StringBuilder regex;

    GlobToRegexParser(String glob, char separator) {
        this.glob = checkNotNull(glob, "glob must be non-null");
        this.separator = separator;
    }

    /**
     * Parses this glob to create an equivalent regular expression that matches
     * the same inputs.
     *
     * @return the regular expression
     */
    public String parseToRegex() {
        if (regex == null) {
            regex = new StringBuilder("^");
            parseGlob();
            regex.append('$');
        }
        return regex.toString();
    }

    private void parseGlob() {
        while (index < glob.length()) {
            char c = glob.charAt(index++);
            if (c == '[') {
                parseBracketExpression();
            } else if (c == '{') {
                parseGroup();
            } else {
                parseChar(c);
            }
        }
    }

    private void parseChar(char c) {
        if (c == '*') {
            if (peek() == '*') {
                regex.append(".*");
                index++;
            } else {
                regex.append("[^" + separator + "]*");
            }
        } else if (c == '?') {
            regex.append("[^" + separator + "]");
        } else if (c == '\\') {
            if (peek() < 0) {
                throw syntaxError("no escaped character");
            } else if (!isGlobMetaChar(glob.charAt(index))) {
                throw syntaxError("character is not escapable", index);
            } else {
                escapeAndAppendChar(glob.charAt(index));
                index++;
            }
        } else {
            escapeAndAppendChar(c);
        }
    }

    private void parseBracketExpression() {
        regex.append("[[^" + separator + "]&&[");

        int first = peek();
        if (first == '!') {
            regex.append('^');
            index++;
        } else if (first == '^') {
            // '^' only needs escaping at the start of a character class
            regex.append('\\').append('^');
            index++;
        } else if (first == ']') {
            throw syntaxError("empty bracket expression");
        }

        // '-' matches itself only at start of expression
        if (first == '-' || (first == '!' && peek() == '-')) {
            regex.append('-');
            index++;
        }

        char c = 0;
        char last = 0;
        boolean rangeAllowed = false;
        while (index < glob.length()) {
            c = glob.charAt(index++);
            if (c == ']') {
                break;
            } else if (c == separator) {
                throw syntaxError("separator in bracket expression");
            } else if (c == '-') {
                if (!rangeAllowed) {
                    throw syntaxError("invalid range");
                }
                regex.append('-');
                if (peek() < 0 || peek() == ']') {
                    break;
                }
                if (peek() < last) {
                    throw syntaxError("invalid range", index - 2);
                }
                rangeAllowed = false;
            } else {
                if (c == '[' || c == '\\' || (c == '&' && peek() == '&')) {
                    regex.append('\\');
                }
                regex.append(c);
                last = c;
                rangeAllowed = true;
            }
        }
        if (c != ']') {
            throw syntaxError("unterminated bracked expression");
        }
        regex.append("]]");
    }

    private void parseGroup() {
        regex.append("(?:");
        char c = 0;
        while (index < glob.length()) {
            c = glob.charAt(index++);
            if (c == '}') {
                break;
            } else if (c == '{') {
                throw syntaxError("nested group");
            } else if (c == ',') {
                regex.append('|');
            } else if (c == '[') {
                parseBracketExpression();
            } else {
                parseChar(c);
            }
        }
        if (c != '}') {
            throw syntaxError("unterminated group");
        }
        regex.append(')');
    }

    /**
     * Returns the character code at {@code index} or -1 there are no characters
     * left in {@code glob}.
     */
    private int peek() {
        return (index < glob.length()) ? glob.charAt(index) : -1;
    }

    private void escapeAndAppendChar(char c) {
        if (REGEX_META_CHARS.indexOf(c) >= 0) {
            regex.append('\\');
        }
        regex.append(c);
    }

    private static boolean isGlobMetaChar(char c) {
        return GLOB_META_CHARS.indexOf(c) >= 0;
    }

    /**
     * Throws a new {@link PatternSyntaxException} with the given description
     * and the current index.
     */
    private PatternSyntaxException syntaxError(String description) {
        return syntaxError(description, index - 1);
    }

    /**
     * Throws a new {@link PatternSyntaxException} with the given description
     * and given index.
     */
    private PatternSyntaxException syntaxError(String description, int errorIndex) {
        throw new PatternSyntaxException(description, glob, errorIndex);
    }
}
