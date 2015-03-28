package com.palantir.giraffe.host;

import java.io.IOException;

/**
 * Provides access to a remote host by opening file systems and execution
 * systems.
 *
 * @author bkeyes
 *
 * @param <C> the type of {@link Credential} used to access the host.
 */
public interface RemoteHostAccessor<C extends Credential<?>> {

    /**
     * Returns the {@link Host} that this object makes accessible.
     */
    Host getHost();

    /**
     * Returns the {@link Credential} used to authenticate with this host.
     */
    C getCredential();

    /**
     * Opens a new host control system on this host. The returned system should be
     * closed after use. It is implementation specific if multiple host control systems
     * can be open for the same host at the same time.
     *
     * @return an open {@link HostControlSystem}
     *
     * @throws IOException if an I/O error occurs while opening the system
     */
    HostControlSystem openHostControlSystem() throws IOException;

}
