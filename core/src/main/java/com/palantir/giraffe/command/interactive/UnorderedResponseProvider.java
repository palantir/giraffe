/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
package com.palantir.giraffe.command.interactive;

import javax.annotation.CheckForNull;

/**
 * An implementation of {@link AbstractResponseProvider} that matches &ltprompt,
 * response&gt pairs in any order. Only one &ltprompt, response&gt pair may
 * match any given input prompt, but any &ltprompt, response&gt pair may be
 * matched against any number of times.
 *
 * @param <T> The type of the response
 *
 * @author alake
 **/
public final class UnorderedResponseProvider<T> extends AbstractResponseProvider<T> {

    /**
     * A builder for an {@code UnorderedResponseProvider}.
     *
     * @author alake
     *
     * @param <T> The type of the response
     */
    public static final class Builder<T> extends AbstractResponseProvider.Builder<T> {
        private Builder() {
        }

        @Override
        public UnorderedResponseProvider<T> build() {
            return new UnorderedResponseProvider<T>(this);
        }
    }

    /*
     * Class definition
     */

    private UnorderedResponseProvider(Builder<T> builder) {
        super(builder);
    }

    /**
     * @return A {@code Builder} for an {@code UnoderedResponseProvider}.
     */
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    @Override
    @CheckForNull
    public T lookupResponse(String token) {
        int numPromptsMatched = 0;
        T response = null;
        for (OutputMatcher<T> matcher : matchers()) {
            if (matcher.tokenMatches(token)) {
                numPromptsMatched++;
                response = matcher.getResponse(token);
            }
        }

        if (numPromptsMatched > 1) {
            throw new IllegalStateException(
                    token + " matched by more than one prompt (" + numPromptsMatched + ").");
        }

        return response;
    }
}
