package com.palantir.giraffe.ssh.internal.base;

import java.net.URI;

import org.slf4j.Logger;

/**
 * Contains information required to create new SSH file and execution systems.
 *
 * @author bkeyes
 */
public class SshSystemContext {

    private final URI uri;
    private final SharedSshClient client;
    private final Logger logger;

    public SshSystemContext(URI uri, SharedSshClient client, Logger logger) {
        this.uri = uri;
        this.client = client;
        this.logger = logger;
    }

    public URI getUri() {
        return uri;
    }

    public SharedSshClient getClient() {
        return client;
    }

    public Logger getLogger() {
        return logger;
    }

}
