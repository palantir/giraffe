package com.palantir.giraffe.ssh;

import java.io.IOException;

import com.palantir.giraffe.host.Authenticator;

/**
 * Implementations of this interface authenticate SSH connections given
 * appropriate credentials.
 * <p>
 * This class participates in the visitor pattern with
 * {@link AbstractSshCredential}.
 *
 * @author bkeyes
 */
public interface SshAuthenticator extends Authenticator {

    void authByPassword(AbstractSshCredential credential, char[] password) throws IOException;

    void authByPublicKey(AbstractSshCredential credential, byte[] privateKey) throws IOException;

}
