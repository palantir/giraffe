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
