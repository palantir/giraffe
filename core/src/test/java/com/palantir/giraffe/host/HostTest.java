package com.palantir.giraffe.host;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

/**
 * Tests basic functionality of {@link Host} methods.
 *
 * @author bkeyes
 */
public class HostTest {

    @Test
    public void loopbackResolvesToLocalhost() {
        Host loopback = Host.fromInetAddress(InetAddress.getLoopbackAddress());
        assertThat(loopback, sameInstance(Host.localhost()));
    }

    @Test
    public void localhostResolvesToLocalhost() {
        Host localhost = Host.fromHostname("localhost");
        assertThat(localhost, sameInstance(Host.localhost()));
    }

    @Test
    public void ipResolvesToLocalhost() throws UnknownHostException {
        InetAddress localAddress = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        assertThat(Host.fromInetAddress(localAddress), sameInstance(Host.localhost()));
    }

}
