package com.palantir.giraffe.host;

import java.io.IOException;

/**
 * Checked exception thrown when authentication fails.
 *
 * @author bkeyes
 */
public class AuthenticationException extends IOException {

    /**
     * Creates an exception with the specified hostname and detail message.
     *
     * @param hostname the hostname of the host on which authentication failed
     * @param message the detail message
     */
    public AuthenticationException(String hostname, String message) {
        this(hostname, message, null);
    }

    /**
     * Creates an exception with the specified hostname, detail message, and
     * cause.
     *
     * @param hostname the hostname of the host on which authentication failed
     * @param message the detail message
     * @param cause the cause of the authentication failure
     */
    public AuthenticationException(String hostname, String message, Throwable cause) {
        super("failed to authenticate to " + hostname + ": " + message, cause);
    }

    private static final long serialVersionUID = 6184687007850310201L;
}
