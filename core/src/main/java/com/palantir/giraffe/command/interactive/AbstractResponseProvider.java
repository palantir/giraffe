/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
package com.palantir.giraffe.command.interactive;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import com.palantir.giraffe.command.interactive.ResponseProvider.OutputMatcher;

/**
 * Abstract implementation of {@link ResponseProvider}. Provides a useful
 * builder
 *
 * @param <T> The type of the response
 *
 * @author alake
 **/
public abstract class AbstractResponseProvider<T> implements ResponseProvider<T> {
    /**
     * A builder for an {@link AbstractResponseProvider}.
     *
     * @author alake
     *
     * @param <T> The type of the response
     */
    public abstract static class Builder<T> {
        private final List<OutputMatcher<T>> outputMatchers = new ArrayList<>();

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt
         * exactly and replies exactly.
         *
         * @param exactPrompt The exact string to match
         * @param exactResponse The exact response to reply with
         * @return this {@code Builder}
         */
        public Builder<T> addExact(
                String exactPrompt,
                T exactResponse) {
            checkNotNull(exactPrompt);
            checkNotNull(exactResponse);
            return addExact(
                    exactPrompt,
                    constantFunction(exactResponse));
        }

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt using
         * a regular expression and replies exactly.
         *
         * @param regexPrompt The regular expression that a prompt must match
         *
         * @param exactResponse The exact response to reply with
         *
         * @return this {@code Builder}
         */
        public Builder<T> addRegex(
                String regexPrompt,
                T exactResponse) {
            checkNotNull(regexPrompt);
            checkNotNull(exactResponse);
            return addRegex(
                    regexPrompt,
                    constantFunction(exactResponse));
        }

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt
         * exactly and replies dynamically.
         *
         * @param exactPrompt The exact string that a prompt must match
         *
         * @param dynamicResponse The {@code Function<String, T>} to call to get
         *        the response to the prompt
         *
         * @return this {@code Builder}
         */
        public Builder<T> addExact(
                String exactPrompt,
                Function<String, T> dynamicResponse) {
            checkNotNull(exactPrompt);
            checkNotNull(dynamicResponse);
            return addPredicate(
                    Predicates.equalTo(exactPrompt),
                    dynamicResponse);
        }

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt using
         * a regular expression and replies dynamically.
         *
         * @param regexPrompt The regular expression that a prompt must match
         *
         * @param dynamicResponse The {@code Function<String, T>} to call to get
         *        the response to the prompt
         *
         * @return this {@code Builder}
         */
        public Builder<T> addRegex(
                String regexPrompt,
                Function<String, T> dynamicResponse) {
            checkNotNull(regexPrompt);
            checkNotNull(dynamicResponse);
            return addPredicate(
                    regexPredicate(regexPrompt),
                    dynamicResponse);
        }

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt
         * dynamically and replies exactly.
         *
         * @param dynamicPrompt The {@code Predicate<String>} used to determine
         *        if a prompt matches
         * @param exactResponse The exact response to reply with
         *
         * @return this {@code Builder}
         */
        public Builder<T> addPredicate(
                Predicate<String> dynamicPrompt,
                T exactResponse) {
            checkNotNull(dynamicPrompt);
            checkNotNull(exactResponse);
            return addPredicate(
                    dynamicPrompt,
                    constantFunction(exactResponse));
        }

        /**
         * Creates a new {@link OutputMatcher} entry that matches a prompt
         * dynamically and replies dynamically.
         *
         * @param dynamicPrompt The {@code Predicate<String>} used to determine
         *        if a prompt matches
         * @param dynamicResponse The {@code Function<String, T>} to call to get
         *        the response to the prompt
         *
         * @return this {@code Builder}
         */
        public Builder<T> addPredicate(
                final Predicate<String> dynamicPrompt,
                final Function<String, T> dynamicResponse) {
            checkNotNull(dynamicPrompt);
            checkNotNull(dynamicResponse);
            return add(new OutputMatcher<T>() {
                @Override
                public boolean tokenMatches(String token) {
                    return dynamicPrompt.apply(token);
                }

                @Override
                public T getResponse(String matchedToken) {
                    return dynamicResponse.apply(matchedToken);
                }
            });
        }

        /**
         * Adds the given {@link OutputMatcher} entry.
         *
         * @param outputMatcher The {@code OutputMatcher} to add
         *
         * @return this {@code Builder}
         */
        public Builder<T> add(OutputMatcher<T> outputMatcher) {
            checkNotNull(outputMatcher);
            outputMatchers.add(outputMatcher);
            return this;
        }

        /**
         * @return A new {@link AbstractResponseProvider}
         */
        public abstract AbstractResponseProvider<T> build();
    }

    private final ImmutableList<OutputMatcher<T>> outputMatchers;

    protected AbstractResponseProvider(Builder<T> builder) {
        this.outputMatchers = ImmutableList.copyOf(builder.outputMatchers);
    }

    /**
     * @return The {@code OutputMatchers} of this {@code ResponseProvider}. The
     *         result is ordered and the same ordering is returned every time.
     */
    protected final ImmutableList<OutputMatcher<T>> matchers() {
        return outputMatchers;
    }

    private static Predicate<String> regexPredicate(final String regex) {
        return new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input.matches(regex);
            }
        };
    }

    private static <T> Function<String, T> constantFunction(final T exactResponse) {
        return new Function<String, T>() {
            @Override
            public T apply(@Nullable String input) {
                return exactResponse;
            }
        };
    }
}
