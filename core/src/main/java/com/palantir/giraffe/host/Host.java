/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                return new Host(ia.getCanonicalHostName());
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

    /**
     * Creates a {@code Host} from a valid hostname. The hostname is resolved to
     * an {@code InetAddress}, which is used to
     * {@linkplain #fromInetAddress(InetAddress) create the host}.
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
     * the local host object. Otherwise, use the host component of the URI.
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
     *         resolved
     */
    public static Host localhost() {
        return LocalHostHolder.localhost;
    }

    /**
     * Creates a {@code Host} from an {@link InetAddress} using the saved or
     * resolved {@link InetAddress#getHostName() hostname}.
     *
     * @param ia the {@code InetAddress}
     */
    public static Host fromInetAddress(InetAddress ia) {
        if (ia.isLoopbackAddress()) {
            return localhost();
        } else {
            String hostname = ia.getHostName();
            if (hostname.equals(LOCALHOST)) {
                return localhost();
            } else {
                return new Host(hostname);
            }
        }
    }

    /**
     * Creates a {@code Host} with the given literal hostname. This method does
     * not perform <a href="#localhost-handling">localhost resolution</a> and
     * {@link #getHostname()} always returns the given hostname.
     *
     * @param name the hostname
     */
    public static Host fromHostnameUnresolved(String name) {
        return new Host(name);
    }

    private final String hostname;

    private Host(String hostname) {
        this.hostname = checkNotNull(hostname, "hostname must be non-null");
        checkArgument(!hostname.isEmpty(), "hostname must not be empty");
    }

    /**
     * Returns the hostname of this host. The hostname is set at construction
     * time and may not be in canonical form.
     *
     * @see #getCanonicalHostname()
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Resolves this host's {@linkplain #getHostname() hostname} to an IP
     * address.
     *
     * @return an {@link InetAddress} for this host
     *
     * @throws InetAddressUnresolvableException if the hostname cannot be
     *         resolved
     */
    public InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new InetAddressUnresolvableException(hostname, e);
        }
    }

    /**
     * Gets this host's canonical hostname by performing a reverse DNS lookup
     * on this host's {@linkplain #getInetAddress() IP address}.
     *
     * @see InetAddress#getCanonicalHostName()
     */
    public String getCanonicalHostname() {
        return getInetAddress().getCanonicalHostName();
    }

    /**
     * Determines if this host has the same canonical name as another host.
     *
     * @param other the other host
     *
     * @return {@code true} if the hosts have the same canonical hostname
     *
     * @throws InetAddressUnresolvableException if either hostname cannot be
     *         resolved
     */
    public boolean resolvesToHost(Host other) {
        return getCanonicalHostname().equals(other.getCanonicalHostname());
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
