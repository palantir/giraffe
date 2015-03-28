package com.palantir.giraffe.ssh.internal;

import com.palantir.giraffe.file.base.ImmutableListPathCore;
import com.palantir.giraffe.ssh.internal.base.BaseSshFileSystem;
import com.palantir.giraffe.ssh.internal.base.BaseSshFileSystemProvider;
import com.palantir.giraffe.ssh.internal.base.SshSystemContext;

final class SshFileSystem extends BaseSshFileSystem<SshPath> {

    SshFileSystem(BaseSshFileSystemProvider<SshPath> provider, SshSystemContext context) {
        super(provider, context);
    }

    @Override
    protected SshPath newPath(ImmutableListPathCore core) {
        return new SshPath(this, core);
    }
}
