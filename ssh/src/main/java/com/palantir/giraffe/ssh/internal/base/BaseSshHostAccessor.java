package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Map;

import com.palantir.giraffe.SystemConverter;
import com.palantir.giraffe.command.ExecutionSystem;
import com.palantir.giraffe.file.base.SuppressedCloseable;
import com.palantir.giraffe.host.AbstractHostControlSystem;
import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.RemoteHostAccessor;
import com.palantir.giraffe.ssh.AbstractSshCredential;

/**
 * Abstract {@code RemoteHostAccessor} implementation for SSH-based systems.
 *
 * @author bkeyes
 *
 * @param <C> the type of credential used to access the host
 */
public abstract class BaseSshHostAccessor<C extends AbstractSshCredential> implements
        RemoteHostAccessor<C> {

    private final Host host;
    private final int port;
    private final C credential;

    protected BaseSshHostAccessor(Host host, int port, C credential) {
        this.host = host;
        this.port = port;
        this.credential = credential;
    }

    @Override
    public Host getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public C getCredential() {
        return credential;
    }

    public abstract URI getSystemUri();

    public Map<String, ?> getSystemEnvironment() {
        return SshEnvironments.makeEnv(this);
    }

    @Override
    public HostControlSystem openHostControlSystem() throws IOException {
        FileSystem fs = FileSystems.newFileSystem(
                getSystemUri(),
                getSystemEnvironment(),
                getClass().getClassLoader());

        // use the file system as the canonical system source
        return new SshHostControlSystem(host, fs, SystemConverter.asExecutionSystem(fs));
    }

    private static final class SshHostControlSystem extends AbstractHostControlSystem {
        private SshHostControlSystem(Host host, FileSystem fs, ExecutionSystem es) {
            super(host, fs, es);
        }

        @Override
        public void close() throws IOException {
            SuppressedCloseable.create(getExecutionSystem(), getFileSystem()).close();
        }
    }
}
