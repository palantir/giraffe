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
package com.palantir.giraffe.ssh.internal;

import static com.google.common.base.Preconditions.checkArgument;

import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.host.HostAccessor;
import com.palantir.giraffe.host.SystemRequest;
import com.palantir.giraffe.host.spi.HostAccessorProvider;
import com.palantir.giraffe.ssh.SshCredential;
import com.palantir.giraffe.ssh.SshHostAccessor;
import com.palantir.giraffe.ssh.SshSystemRequest;

/**
 * Provides access to remote host control systems using SSH.
 *
 * @author bkeyes
 */
public class SshHostAccessorProvider extends HostAccessorProvider {

    @Override
    public String getScheme() {
        return SshUris.getHostScheme();
    }

    @Override
    public HostAccessor newAccessor(SystemRequest request) {
        SshUris.checkHostUri(request.uri());
        checkArgument(request instanceof SshSystemRequest,
                "request must be an instance of %s",
                SshSystemRequest.class.getName());

        SshSystemRequest sshRequest = (SshSystemRequest) request;

        Host host = Host.fromUri(request.uri());
        int port = sshRequest.getPort();
        SshCredential credential = sshRequest.getCredential();

        return SshHostAccessor.forCredential(host, port, credential);
    }

}
