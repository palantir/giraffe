package com.palantir.giraffe.host;

import java.io.IOException;

/**
 * Contains information required to authenticate a connection to a remote host
 * or service.
 *
 * @author bkeyes
 *
 * @param <A> the type of {@link Authenticator} that accepts this credential
 */
public abstract class Credential<A extends Authenticator> {

    private final Class<A> authenticatorType;

    protected Credential(Class<A> authenticatorType) {
        this.authenticatorType = authenticatorType;
    }

    /**
     * Returns the type of {@link Authenticator} accepted by this credential.
     */
    public final Class<A> getAuthenticatorType() {
        return authenticatorType;
    }

    /**
     * Authenticates the connection managed by the given {@link Authenticator}
     * using this credential.
     * <p>
     * This method participates in the visitor pattern with methods defined by
     * {@code Authenticator} implementations.
     *
     * @param authenticator the {@code Authenticator} for the connection
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs while authenticating the
     *         connection
     */
    public abstract void authenticate(A authenticator) throws IOException;
}
