/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
package com.palantir.giraffe.command.interactive;

import java.util.Iterator;

import javax.annotation.CheckForNull;

/**
 * An implementation of {@link AbstractResponseProvider} that matches &ltprompt,
 * response&gt pairs sequentially. This means that if A and B are &ltprompt,
 * response&gt pairs and A precedes B, the prompt for B will not be matched
 * until the prompt for A is first matched. Moreover, each &ltprompt,
 * response&gt pair will only ever be matched (and subsequently respond) only
 * once.
 *
 * @param <T> The type of the response
 *
 * @author alake
 **/
public final class OrderedResponseProvider<T> extends AbstractResponseProvider<T> {

    /**
     * A builder for an {@code OrderedResponseProvider}.
     *
     * @author alake
     *
     * @param <T> The type of the response
     */
    public static final class Builder<T> extends AbstractResponseProvider.Builder<T> {
        private Builder() {
        }

        @Override
        public AbstractResponseProvider<T> build() {
            return new OrderedResponseProvider<T>(this);
        }
    }

    /*
     * Class definition
     */

    /**
     * @return A {@code Builder} for an {@code OrderedResponseProvider}.
     */
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    @CheckForNull
    private OutputMatcher<T> currPromptResponse;
    private final Iterator<OutputMatcher<T>> matcherIter;

    private OrderedResponseProvider(Builder<T> builder) {
        super(builder);
        this.matcherIter = matchers().iterator();
        this.currPromptResponse = matcherIter.hasNext() ? matcherIter.next() : null;
    }

    @Override
    @CheckForNull
    public T lookupResponse(String token) {
        if (currPromptResponse == null) {
            return null;
        }

        if (currPromptResponse.tokenMatches(token)) {
            T reply = currPromptResponse.getResponse(token);
            currPromptResponse = matcherIter.hasNext() ? matcherIter.next() : null;
            return reply;
        }

        return null;
    }
}
