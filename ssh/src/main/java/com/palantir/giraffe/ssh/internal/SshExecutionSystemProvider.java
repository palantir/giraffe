package com.palantir.giraffe.ssh.internal;

import java.net.URI;

import com.palantir.giraffe.ssh.internal.base.BaseSshExecutionSystemProvider;
import com.palantir.giraffe.ssh.internal.base.SshConnectionFactory;
import com.palantir.giraffe.ssh.internal.base.SshSystemContext;

import net.schmizz.sshj.DefaultConfig;

/**
 * Provides access to remote execution systems using SSH.
 *
 * @author bkeyes
 */
public final class SshExecutionSystemProvider extends BaseSshExecutionSystemProvider<SshCommand> {

    private final SshConnectionFactory connectionFactory;

    public SshExecutionSystemProvider() {
        super(SshCommand.class);
        connectionFactory = new SshConnectionFactory(new DefaultConfig());
    }

    @Override
    public SshExecutionSystem newExecutionSystem(SshSystemContext context) {
        return new SshExecutionSystem(this, context);
    }

    @Override
    public String getScheme() {
        return SshUris.getUriScheme();
    }

    @Override
    protected void checkUri(URI uri) {
        SshUris.checkUri(uri);
    }

    @Override
    protected SshConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
