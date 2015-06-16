package com.palantir.giraffe.file.base;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Globs#toRegex(String, String)}. Test the parser by checking that
 * the regex matches inputs correctly instead of checking that the regex equals
 * some expected string.
 *
 * @author bkeyes
 */
public class GlobToRegexTest {

    protected static final String SEPARATOR = "/";

    @Test
    public void basicLiterals() {
        assertPattern("console.log", matches("console.log"));
        assertPattern("console.log", notMatches("error.log"));

        assertPattern("foo/bar/console.log", matches("foo/bar/console.log"));
        assertPattern("foo/bar/console.log", notMatches("foo/caz/console.log"));
    }

    @Test
    public void matchesOnFullString() {
        assertPattern("console.log", notMatches("foo/bar/console.log"));
        assertPattern("foo/bar/console.log", notMatches("console.log"));
    }

    @Test
    public void literalsAreCaseSensitive() {
        assertPattern("console.log", notMatches("Console.log"));
    }

    @Test
    public void matchesLiteralRegexMetaChars() {
        assertPattern("^(...|abc)+$", matches("^(...|abc)+$"));

        assertPattern("^(...|abc)+$", notMatches("123xyzabc"));
        assertPattern("^(...|abc)+$", notMatches("abc"));
        assertPattern("^(...|abc)+$", notMatches("..."));
    }

    @Test
    public void starMatchesEmptyString() {
        assertPattern("*", matches(""));
    }

    @Test
    public void starDoesNotMatchSeparator() {
        assertPattern("*", notMatches(SEPARATOR));
    }

    @Test
    public void starMatchesSingleCharacter() {
        assertPattern("*", matches("a"));
        assertPattern("*", matches("."));
        assertPattern("*", matches("*"));
    }

    @Test
    public void starMatchesMultipleChars() {
        assertPattern("*", matches("abcd"));
    }

    @Test
    public void singleStarMatchesAny() {
        assertPattern("*.java", matches("foo.java"));
        assertPattern("*.java", matches("bar.java"));
        assertPattern("foo.*", matches("foo.java"));
        assertPattern("foo.*", matches("foo.log"));
        assertPattern("hello*world", matches("hello world"));
        assertPattern("hello*world", matches("helloawesomeworld"));

        assertPattern("*.java", notMatches("foo.cpp"));
        assertPattern("foo.*", notMatches("bar.log"));
        assertPattern("hello*world", notMatches("goodbye world"));
    }

    @Test
    public void multipleStarMatchesAny() {
        assertPattern("*test*", matches("footestbar"));
        assertPattern("*.*", matches("foo.java"));
        assertPattern("*.*", matches(".bashrc"));
        assertPattern("foo/*/bar/*/file.log", matches("foo/a/bar/b/file.log"));
        assertPattern("foo/*/bar/*/file.log", matches("foo/c/bar/d/file.log"));

        assertPattern("*test*", notMatches("foobazbar"));
        assertPattern("*.*", notMatches("config"));
        assertPattern("foo/*/bar/*/file.log", notMatches("foo/c/baz/d/file.log"));
    }

    @Test
    public void starStarMatchesEmptyString() {
        assertPattern("**", matches(""));
    }

    @Test
    public void starStartMatchesSeparator() {
        assertPattern("**", matches(SEPARATOR));
    }

    @Test
    public void singleStarStarMatchesAny() {
        assertPattern("**.java", matches("foo.java"));
        assertPattern("**.java", matches("foo/bar.java"));
        assertPattern("foo.**", matches("foo.java"));
        assertPattern("foo.**", matches("foo.log/1.zip"));
        assertPattern("foo/**/file.log", matches("foo/bar/file.log"));
        assertPattern("foo/**/file.log", matches("foo/bar/baz/file.log"));

        assertPattern("**.java", notMatches("foo.cpp"));
        assertPattern("foo.**", notMatches("bar.log"));
        assertPattern("foo/**/file.log", notMatches("foo/bar/baz/file.java"));
    }

    @Test
    public void multipleStarStarMatchesAny() {
        assertPattern("**test**", matches("footestbar/baz/biff"));
        assertPattern("**.**", matches("foo.java"));
        assertPattern("**.**", matches("init.d/apache2"));
        assertPattern("foo/**/bar/**/file.log", matches("foo/a/bar/b/c/file.log"));

        assertPattern("**test**", notMatches("foobar/baz/biff"));
        assertPattern("**.**", notMatches("etc/config"));
        assertPattern("foo/**/bar/**/file.log", notMatches("foo/c/baz/d/e/file.log"));
    }

    @Test
    public void questionDoesNotMatchEmptyString() {
        assertPattern("?", notMatches(""));
    }

    @Test
    public void questionDoesNotMatchSeparator() {
        assertPattern("?", notMatches(SEPARATOR));
    }

    @Test
    public void questionMatchesSingleCharacter() {
        assertPattern("?", matches("a"));
        assertPattern("?", matches("."));
        assertPattern("?", matches("?"));
    }

    @Test
    public void questionDoesNotMatchMultipleCharaters() {
        assertPattern("?", notMatches("abcd"));
    }

    @Test
    public void singleQuestionMatchesAny() {
        assertPattern("?oo", matches("foo"));
        assertPattern("?oo", matches("boo"));
        assertPattern("ba?", matches("bar"));
        assertPattern("ba?", matches("baz"));
        assertPattern("b?z", matches("baz"));
        assertPattern("b?z", matches("biz"));

        assertPattern("?oo", notMatches("bop"));
        assertPattern("?oo", notMatches("igloo"));
        assertPattern("ba?", notMatches("biz"));
        assertPattern("ba?", notMatches("barb"));
        assertPattern("b?z", notMatches("bar"));
        assertPattern("b?z", notMatches("beez"));
    }

    @Test
    public void multipleQuestionMatchAny() {
        assertPattern("?o?", matches("foo"));
        assertPattern("?o?", matches("fog"));
        assertPattern("b??r", matches("bear"));
        assertPattern("b??r", matches("beer"));
        assertPattern("b?z b?z", matches("biz baz"));

        assertPattern("?o?", notMatches("fro"));
        assertPattern("b??r", notMatches("beef"));
        assertPattern("b?z b?z", notMatches("baz bar"));
    }

    @Test
    public void escapedMetaCharacterIsLiteral() {
        assertPattern("\\{", matches("{"));
        assertPattern("\\*", notMatches("a"));
    }

    @Test(expected = PatternSyntaxException.class)
    public void trailingEscapeThrowsException() {
        Globs.toRegex("test\\", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void escapingNormalCharacterThrowsException() {
        Globs.toRegex("\\a", SEPARATOR);
    }

    @Test
    public void escapedCharacterInPattern() {
        assertPattern("C:\\\\*", matches("C:\\foo"));
        assertPattern("C:\\\\*", notMatches("C:/foo"));
    }

    @Test
    public void simpleBracketExpressionMatchesChars() {
        assertPattern("[abc]", matches("a"));
        assertPattern("[abc]", matches("b"));
        assertPattern("[abc]", matches("c"));

        assertPattern("[abc]", notMatches("d"));
    }

    @Test
    public void singleRangeBracketExpressionMatchesChars() {
        assertPattern("[a-c]", matches("a"));
        assertPattern("[a-c]", matches("b"));
        assertPattern("[a-c]", matches("c"));

        assertPattern("[a-c]", notMatches("d"));
    }

    @Test
    public void dualRangeBracketExpressionMatchesChars() {
        assertPattern("[a-cx-z]", matches("b"));
        assertPattern("[a-cx-z]", matches("y"));
        assertPattern("[a-cx-z]", matches("a"));
        assertPattern("[a-cx-z]", matches("z"));

        assertPattern("[a-cx-z]", notMatches("d"));
        assertPattern("[a-cx-z]", notMatches("w"));
    }

    @Test
    public void rangeAndLiteralBracketExpressionMatchesChars() {
        assertPattern("[A-C248]", matches("B"));
        assertPattern("[A-C248]", matches("4"));
        assertPattern("[A-C248]", matches("A"));
        assertPattern("[A-C248]", matches("8"));

        assertPattern("[A-C248]", notMatches("a"));
        assertPattern("[A-C248]", notMatches("D"));
        assertPattern("[A-C248]", notMatches("3"));
    }

    @Test
    public void negatedSimpleBracketExpressionDoesNotMatchChars() {
        assertPattern("[!abc]", notMatches("a"));
        assertPattern("[!abc]", notMatches("b"));
        assertPattern("[!abc]", notMatches("c"));

        assertPattern("[!abc]", matches("1"));
    }

    @Test
    public void negatedRangeBracketExpressionDoesNotMatchChars() {
        assertPattern("[!a-c]", notMatches("a"));
        assertPattern("[!a-c]", notMatches("b"));
        assertPattern("[!a-c]", notMatches("c"));

        assertPattern("[!a-c]", matches("1"));
    }

    @Test
    public void bracketExpressionEscapesRegexMetaChars() {
        assertPattern("[^a]", matches("a"));
        assertPattern("[^a]", matches("^"));

        assertPattern("[^a]", notMatches("b"));
    }

    @Test
    public void bracketExpressionMatchesGlobMetaChars() {
        assertPattern("[*?\\]", matches("*"));
        assertPattern("[*?\\]", matches("?"));
        assertPattern("[*?\\]", matches("\\"));

        assertPattern("[*?\\]", notMatches("a"));
    }

    @Test
    public void bracketExpressionInPattern() {
        assertPattern("*.[cho]", matches("io.c"));
        assertPattern("*.[cho]", matches("io.o"));
        assertPattern("*.[cho]", matches("io.h"));

        assertPattern("*.[cho]", notMatches("io.S"));
    }

    @Test(expected = PatternSyntaxException.class)
    public void emptyBracketExpressionThrowsException() {
        Globs.toRegex("[]", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void unterminatedBracketExpressionThrowsException() {
        Globs.toRegex("[abc", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void unterminatedRangeInBracketExpressionThrowsException() {
        Globs.toRegex("[ab-]", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void reverseRangeInBracketExpressionThrowsException() {
        Globs.toRegex("[z-a]", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void separatorInBracketExpressionThrowsException() {
        Globs.toRegex("[ab" + SEPARATOR + "]", SEPARATOR);
    }

    @Test
    public void emptyGroupMatchesEmptyString() {
        assertPattern("{}", matches(""));
    }

    @Test
    public void emptyGroupMatchesNoCharacters() {
        assertPattern("{}", notMatches("a"));
        assertPattern("{}", notMatches("hello"));
    }

    @Test
    public void singlePatternGroupMatchesPattern() {
        assertPattern("{a}", matches("a"));
        assertPattern("{a}", notMatches("b"));
    }

    @Test
    public void literalsInGroup() {
        assertPattern("{java,class}", matches("java"));
        assertPattern("{java,class}", matches("class"));

        assertPattern("{java,class}", notMatches("html"));
    }

    @Test
    public void starInGroup() {
        assertPattern("{*.java,class}", matches("Globs.java"));
        assertPattern("{*.java,class}", matches("class"));

        assertPattern("{*.java,class}", notMatches("Globs.class"));
    }

    @Test
    public void starStarInGroup() {
        assertPattern("{java,**.class}", matches("java"));
        assertPattern("{java,**.class}", matches("bin/classes/Globs.class"));

        assertPattern("{java,**.class}", notMatches("bin/classes/Globs.java"));
    }

    @Test
    public void questionInGroup() {
        assertPattern("{fo?,b?z}", matches("foo"));
        assertPattern("{fo?,b?z}", matches("for"));
        assertPattern("{fo?,b?z}", matches("baz"));
        assertPattern("{fo?,b?z}", matches("biz"));

        assertPattern("{fo?,b?z}", notMatches("food"));
        assertPattern("{fo?,b?z}", notMatches("beez"));
    }

    @Test
    public void backslashInGroup() {
        assertPattern("{\\*.java,class}", matches("*.java"));
        assertPattern("{\\*.java,class}", matches("class"));

        assertPattern("{\\*.java,class}", notMatches("Globs.java"));
    }

    @Test
    public void bracketExpressionInGroup() {
        assertPattern("{ba[rz],#[0-9]}", matches("bar"));
        assertPattern("{ba[rz],#[0-9]}", matches("baz"));
        assertPattern("{ba[rz],#[0-9]}", matches("#4"));
        assertPattern("{ba[rz],#[0-9]}", matches("#0"));

        assertPattern("{ba[rz],#[0-9]}", notMatches("#100"));
        assertPattern("{ba[rz],#[0-9]}", notMatches("bat"));
    }

    @Test
    public void groupInPattern() {
        assertPattern("*.{java,class}", matches("Globs.java"));
        assertPattern("*.{java,class}", matches("Globs.class"));

        assertPattern("*.{java,class}", notMatches("log.properties"));
    }

    @Test
    public void escapeCommaInGroup() {
        assertPattern("{a\\,b,c}", matches("a,b"));
        assertPattern("{a\\,b,c}", matches("c"));

        assertPattern("{a\\,b,c}", notMatches("a"));
        assertPattern("{a\\,b,c}", notMatches("b"));
        assertPattern("{a\\,b,c}", notMatches("a\\,b"));
    }

    @Test
    public void escapeBraceInGroup() {
        assertPattern("{a\\{b\\},c}", matches("a{b}"));
        assertPattern("{a\\{b\\},c}", matches("c"));

        assertPattern("{a\\{b\\},c}", notMatches("ab"));
        assertPattern("{a\\{b\\},c}", notMatches("a\\{b\\}"));
    }

    @Test(expected = PatternSyntaxException.class)
    public void unterminatedGroupThrowsException() {
        Globs.toRegex("{file,dir", SEPARATOR);
    }

    @Test(expected = PatternSyntaxException.class)
    public void nestedGroupThrowsException() {
        Globs.toRegex("{foo,{bar,baz}}", SEPARATOR);
    }

    private static Matcher<String> matches(final String input) {
        return new TypeSafeMatcher<String>() {
            private String regex;

            @Override
            public void describeTo(Description description) {
                description.appendText("match for ").appendValue(input);
            }

            @Override
            protected boolean matchesSafely(String item) {
                regex = Globs.toRegex(item, SEPARATOR);
                return Pattern.compile(regex).matcher(input).matches();
            }

            @Override
            protected void describeMismatchSafely(String item, Description mismatch) {
                mismatch.appendText("no match with regex ").appendValue(regex)
                        .appendText(" from glob ").appendValue(item);
            }
        };
    }

    private static Matcher<String> notMatches(final String input) {
        return new TypeSafeMatcher<String>() {
            private String regex;

            @Override
            public void describeTo(Description description) {
                description.appendText("no match for ").appendValue(input);
            }

            @Override
            protected boolean matchesSafely(String item) {
                regex = Globs.toRegex(item, SEPARATOR);
                return !Pattern.compile(regex).matcher(input).matches();
            }

            @Override
            protected void describeMismatchSafely(String item, Description mismatch) {
                mismatch.appendText("match with regex ").appendValue(regex)
                        .appendText(" from glob ").appendValue(item);
            }
        };
    }


    private static void assertPattern(String pattern, Matcher<String> matcher) {
        Assert.assertThat("regex is not correct", pattern, matcher);
    }
}
