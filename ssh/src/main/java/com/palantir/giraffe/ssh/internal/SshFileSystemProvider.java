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
