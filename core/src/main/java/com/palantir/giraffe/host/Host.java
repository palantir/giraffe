package com.palantir.giraffe.host;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

import com.google.common.collect.Sets;
import com.palantir.giraffe.command.CommandException;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.internal.OsDetector;

/**
 * Represents a host with a unique hostname in a network.
 *
 * <h3><a name="localhost-handling">Localhost Handling</a></h3>
 * <p>
 * This class attempts to resolve all representations of the local host to the
 * same object. The canonical local host object is the one returned by
 * {@link #localhost()}. Factory methods that perform resolution return this
 * object when given any loopback IP address or the string "localhost".
 *
 * @author pchen
 * @author bkeyes
 */
public final class Host {

    private static final String LOCALHOST = "localhost";

    private static final Set<String> localUriSchemes = Sets.newHashSet("file");

    /**
     * Adds a URI scheme to the set used by {@link #fromUri(URI)} to identify
     * the localhost URIs.
     * <p>
     * By default, the {@code file} scheme is included.
     *
     * @param scheme the scheme to add
     */
    public static void addLocalUriScheme(String scheme) {
        checkNotNull(scheme, "scheme must be non-null");
        checkArgument(!scheme.isEmpty(), "scheme must not be empty");
        localUriSchemes.add(scheme);
    }

    private static final class LocalHostHolder {
        private static final Host localhost = getLocalhostInstance();

        private static Host getLocalhostInstance() {
            try {
                InetAddress ia = InetAddress.getLocalHost();
                return new Host(ia.getCanonicalHostName(), ia);
            } catch (UnknownHostException e) {
                // work around JDK-7180557 (Java 7 only)
                // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7180557
                if (OsDetector.isOsX()) {
                    return tryOsXHostnameCommand();
                }
                throw new InetAddressUnresolvableException(LOCALHOST, e);
            }
        }

        private static Host tryOsXHostnameCommand() {
            try {
                String name = Commands.execute(Commands.get("/bin/hostname")).getStdOut().trim();
                return fromHostnameUnresolved(name);
            } catch (IOException | CommandException e) {
                throw new InetAddressUnresolvableException(LOCALHOST, e);
            }
        }
    }

    private final String hostname;
    private volatile InetAddress cachedInetAddress;

    /**
     * Creates a {@code Host} from a hostname, resolving it to an
     * {@link InetAddress} then attempting to resolve the canonical name.
     *
     * @param name the hostname to resolve
     *
     * @throws InetAddressUnresolvableException if the hostname cannot be
     *         resolved
     */
    public static Host fromHostname(String name) {
        if (name.equals(LOCALHOST)) {
            return localhost();
        } else {
            try {
                return fromInetAddress(InetAddress.getByName(name));
            } catch (UnknownHostException e) {
                throw new InetAddressUnresolvableException(name, e);
            }
        }
    }

    /**
     * Gets a {@link Host} from a {@link URI}.
     * <p>
     * If the URI's scheme {@linkplain #addLocalUriScheme(String) identifies}
     * resources on this host or if the host component is "localhost", returns
     * the local host object. Otherwise, use the
     * {@linkplain #fromHostnameUnresolved(String) unresolved} host component of
     * the URI.
     *
     * @param uri the URI from which to extract the host
     *
     * @return a host for the given URI
     *
     * @throws IllegalArgumentException if the URI has no host component and
     *         does not have a known scheme
     */
    public static Host fromUri(URI uri) {
        checkNotNull(uri, "uri must be non-null");
        String scheme = uri.getScheme();
        if (scheme != null && localUriSchemes.contains(scheme.toLowerCase())) {
            return localhost();
        }

        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("URI '" + uri + "' must have a host component");
        } else if (host.equals(LOCALHOST)) {
            return localhost();
        } else {
            return fromHostnameUnresolved(host);
        }
    }

    /**
     * Gets the {@code Host} representing the local host.
     *
     * @throws InetAddressUnresolvableException if "localhost" cannot be
     *         resolved to an externally visible hostname.
     */
    public static Host localhost() {
        return LocalHostHolder.localhost;
    }

    /**
     * Creates a {@code Host} from an {@link InetAddress}, attempting to resolve
     * the canonical host name.
     *
     * @param ia the {@code InetAddress}
     */
    public static Host fromInetAddress(InetAddress ia) {
        if (ia.isLoopbackAddress()) {
            return localhost();
        } else {
            String hostname = ia.getCanonicalHostName();
            if (hostname.equals(LOCALHOST)) {
                return localhost();
            } else {
                return new Host(hostname, ia);
            }
        }
    }

    /**
     * Creates a {@code Host} with the given hostname. No resolution, including
     * <a href="#localhost-handling">local host resolution</a>, is performed and
     * {@link #getHostname()} returns the given hostname.
     *
     * @param name the hostname
     */
    public static Host fromHostnameUnresolved(String name) {
        // TODO(bkeyes): filter for permitted characters?
        return new Host(name, null);
    }

    private Host(String hostname, InetAddress inetAddress) {
        this.hostname = checkNotNull(hostname, "hostname must be non-null");
        checkArgument(!hostname.isEmpty(), "hostname must not be empty");
        this.cachedInetAddress = inetAddress;
    }

    /**
     * Returns the hostname of this host. The hostname is set at construction
     * time and may not be in canonical form or refer to a valid host if this
     * host was created using {@link #fromHostnameUnresolved(String)}.
     *
     * @see #getCanonicalHostName()
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Resolves this host's {@linkplain #getHostname() hostname} to an IP
     * address. Unlike {@code getInetAddress}, resolution is always performed
     * and the result is never saved.
     *
     * @return an {@link InetAddress} for this host
     *
     * @throws InetAddressUnresolvableException if the hostname cannot be
     *         resolved
     *
     * @see #getInetAddress()
     */
    public InetAddress resolveInetAddress() {
        try {
            return InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new InetAddressUnresolvableException(hostname, e);
        }
    }

    /**
     * Gets this host's IP address. If the host was constructed with an IP
     * address, that address is returned. If the host was constructed using
     * {@link #fromHostnameUnresolved(String)}, the IP address is
     * {@linkplain #resolveInetAddress() resolved} on the first call to this
     * method. Subsequent calls return the resolved address.
     *
     * @return an {@link InetAddress} for this host
     *
     * @throws InetAddressUnresolvableException if this host was constructed
     *         without an IP address and the hostname cannot be resolved
     */
    public InetAddress getInetAddress() {
        if (cachedInetAddress == null) {
            cachedInetAddress = resolveInetAddress();
        }
        return cachedInetAddress;
    }

    /**
     * Gets this host's canonical hostname by performing a reverse DNS lookup
     * on this host's {@linkplain #getInetAddress() IP address}.
     *
     * @see InetAddress#getCanonicalHostName()
     */
    public String getCanonicalHostName() {
        return getInetAddress().getCanonicalHostName();
    }

    /**
     * Determines if this host has the same canonical name as another host. The
     * canonical name of the other host is always resolved.
     *
     * @param other the other host
     *
     * @return {@code true} if the other host has the same canonical hostname as
     *         this host
     *
     * @throws InetAddressUnresolvableException if either host name cannot be
     *         resolved
     */
    public boolean resolvesToHost(Host other) {
        // TODO(bkeyes): is there a better resolution contract?
        return getCanonicalHostName().equals(other.resolveInetAddress().getCanonicalHostName());
    }

    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

    /**
     * Determines if this {@code Host} is equal to another {@code Host}. Two
     * {@code Host}s are equal if they have the same {@linkplain #getHostname()
     * hostname}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Host)) {
            return false;
        } else {
            return hostname.equals(((Host) obj).hostname);
        }
    }

    /**
     * Returns the {@linkplain #getHostname() hostname} of this host.
     */
    @Override
    public String toString() {
        return hostname;
    }
}
