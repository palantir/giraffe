package com.palantir.giraffe.ssh;

import java.io.IOException;

/**
 * Authenticates SSH connections using a password.
 *
 * @author bkeyes
 */
public final class PasswordCredential extends AbstractSshCredential {

    private final char[] password;

    public PasswordCredential(String username, char[] password) {
        super(username);
        this.password = password.clone();
    }

    @Override
    public void authenticate(SshAuthenticator authenticator) throws IOException {
        authenticator.authByPassword(this, password.clone());
    }

    public char[] getPassword() {
        return password.clone();
    }
}
