package com.palantir.giraffe.ssh;

import com.palantir.giraffe.host.Credential;

/**
 * Base class for SSH credentials.
 *
 * @author bkeyes
 */
public abstract class AbstractSshCredential extends Credential<SshAuthenticator> {

    private final String username;

    AbstractSshCredential(String username) {
        super(SshAuthenticator.class);
        this.username = username;
    }

    public final String getUsername() {
        return username;
    }
}
