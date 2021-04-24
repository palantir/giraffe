/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */
package com.palantir.giraffe.command.interactive;

import javax.annotation.CheckForNull;

/**
 * Used to get a response, given an instance of an input {@code String}.
 * Implementors should determine the strategy used to produce the response.
 *
 * @param <T> The type of the response
 *
 * @author alake
 **/
public interface ResponseProvider<T> {

    /**
     * Represents a &lt;prompt, response&gt; pair.
     *
     * @author alake
     *
     * @param <T> The type of the response
     */
    public interface OutputMatcher<T> {
        /**
         * @param token The token to test
         * @return true if the token is matches the prompt, false otherwise
         */
        boolean tokenMatches(String token);

        /**
         * @param matchedToken A token which matches the prompt
         * @return The response for {@code matchedToken}
         */
        T getResponse(String matchedToken);
    }

    /**
     * @param input The prompt for which to check for a response
     *
     * @return The response for the input, if any exists, or null if there's no
     *         such response.
     */
    @CheckForNull
    T lookupResponse(String input);
}
