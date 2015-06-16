package com.palantir.giraffe.host;

/**
 * Unchecked exception thrown when a hostname cannot be resolved to an
 * {@link java.net.InetAddress}.
 *
 * @author bkeyes
 */
public final class InetAddressUnresolvableException extends RuntimeException {

    private final String hostname;

    /**
     * Creates an exception with the specified hostname and cause.
     *
     * @param hostname the hostname that could not be resolved
     * @param cause the cause
     */
    public InetAddressUnresolvableException(String hostname, Throwable cause) {
        super(hostname + " could not be resolved", cause);
        this.hostname = hostname;
    }

    /**
     * Returns the hostname that could not be resolved.
     */
    public String getHostname() {
        return hostname;
    }

    private static final long serialVersionUID = 1L;
}
