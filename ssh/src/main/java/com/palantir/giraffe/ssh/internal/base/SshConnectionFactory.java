package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;

import com.palantir.giraffe.ssh.AbstractSshCredential;
import com.palantir.giraffe.ssh.SshAuthenticator;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

/**
 * Creates SSH connections from an environment map.
 *
 * @author bkeyes
 */
public final class SshConnectionFactory {

    private final Config config;

    public SshConnectionFactory(Config sshjConfiguration) {
        this.config = sshjConfiguration;
    }

    public SSHClient newAuthedConnection(BaseSshHostAccessor<?> host) throws IOException {
        SSHClient sshClient = new SSHClient(config);
        sshClient.getTransport().addHostKeyVerifier(new PromiscuousVerifier());

        try {
            sshClient.connect(host.getHost().getHostname(), host.getPort());
            host.getCredential().authenticate(new Authenticator(sshClient));
        } catch (IOException e) {
            IOUtils.closeQuietly(sshClient);
            throw e;
        }

        return sshClient;
    }

    private final class Authenticator implements SshAuthenticator {
        private final SSHClient client;

        public Authenticator(SSHClient client) {
            this.client = client;
        }

        @Override
        public void authByPassword(AbstractSshCredential credential, char[] password)
                throws IOException {
            client.authPassword(credential.getUsername(), password);
        }

        @Override
        public void authByPublicKey(AbstractSshCredential credential, byte[] privateKey)
                throws IOException {
            KeyProvider keyProvider = client.loadKeys(new String(privateKey), null, null);
            client.authPublickey(credential.getUsername(), keyProvider);
        }
    }
}
