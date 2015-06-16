package com.palantir.giraffe.ssh;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.ssh.internal.SshUris;
import com.palantir.giraffe.ssh.internal.base.BaseSshHostAccessor;

/**
 * Provides access to a file system and execution system on a host using SSH.
 *
 * @author bkeyes
 *
 * @param <C> the type of credential used to access the host
 */
public final class SshHost<C extends AbstractSshCredential> extends BaseSshHostAccessor<C> {

    private static final int DEFAULT_SSH_PORT = 22;

    public static SshHost<PasswordCredential> authWithPassword(Host host, String username,
            String password) {
        return authWithCredential(host, new PasswordCredential(username, password.toCharArray()));
    }

    public static SshHost<PasswordCredential> authWithPassword(Host host, String username,
            char[] password) {
        return authWithCredential(host, new PasswordCredential(username, password));
    }

    public static SshHost<PasswordCredential> authWithPassword(Host host, String username,
            int port, char[] password) {
        return authWithCredential(host, port, new PasswordCredential(username, password));
    }

    public static SshHost<PublicKeyCredential> authWithKey(Host host, String username,
            Path privateKeyPath) throws IOException {
        return authWithCredential(host, readKey(username, privateKeyPath));
    }

    public static SshHost<PublicKeyCredential> authWithKey(Host host, String username,
            int port, Path privateKeyPath) throws IOException {
        return authWithCredential(host, port, readKey(username, privateKeyPath));
    }

    private static PublicKeyCredential readKey(String username, Path file) throws IOException {
        return new PublicKeyCredential(username, Files.readAllBytes(file), file);
    }

    public static SshHost<PublicKeyCredential> authWithKey(Host host, String username,
            byte[] privateKey) {
        return authWithCredential(host, new PublicKeyCredential(username, privateKey));
    }

    public static SshHost<PublicKeyCredential> authWithKey(Host host, String username,
            int port, byte[] privateKey) {
        return authWithCredential(host, port, new PublicKeyCredential(username, privateKey));
    }

    public static <C extends AbstractSshCredential> SshHost<C> authWithCredential(Host host,
            C credential) {
        return authWithCredential(host, DEFAULT_SSH_PORT, credential);
    }

    public static <C extends AbstractSshCredential> SshHost<C> authWithCredential(Host host,
            int port, C credential) {
        return new SshHost<C>(host, port, credential);
    }

    private SshHost(Host host, int port, C credential) {
        super(host, port, credential);
    }

    @Override
    public URI getSystemUri() {
        return SshUris.getUri(getHost(), getPort(), getCredential());
    }

}
