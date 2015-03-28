package com.palantir.giraffe.ssh.internal;

import java.net.URI;

import com.palantir.giraffe.ssh.internal.base.BaseSshFileSystemProvider;
import com.palantir.giraffe.ssh.internal.base.SshConnectionFactory;
import com.palantir.giraffe.ssh.internal.base.SshSystemContext;

import net.schmizz.sshj.DefaultConfig;

/**
 * Provides access to a remote file systems using SSH and SFTP.
 *
 * @author bkeyes
 */
public final class SshFileSystemProvider extends BaseSshFileSystemProvider<SshPath> {

    private final SshConnectionFactory connectionFactory;

    public SshFileSystemProvider() {
        super(SshPath.class);
        connectionFactory = new SshConnectionFactory(new DefaultConfig());
    }

    @Override
    public String getScheme() {
        return SshUris.getUriScheme();
    }

    @Override
    protected SshConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    @Override
    protected SshFileSystem newFileSystem(SshSystemContext context) {
        return new SshFileSystem(this, context);
    }

    @Override
    protected void checkUri(URI uri) {
        SshUris.checkUri(uri);
    }

}
