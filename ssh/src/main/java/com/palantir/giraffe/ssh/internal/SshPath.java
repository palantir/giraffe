package com.palantir.giraffe.ssh.internal;

import java.util.Objects;

import com.palantir.giraffe.file.base.ImmutableListPathCore;
import com.palantir.giraffe.ssh.internal.base.BaseSshFileSystem;
import com.palantir.giraffe.ssh.internal.base.BaseSshPath;

final class SshPath extends BaseSshPath<SshPath> {

    SshPath(BaseSshFileSystem<SshPath> fs, ImmutableListPathCore core) {
        super(fs, core);
    }

    @Override
    protected SshPath newPath(ImmutableListPathCore newCore) {
        return new SshPath(getFileSystem(), newCore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileSystem(), toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SshPath)) {
            return false;
        } else {
            SshPath that = (SshPath) obj;
            return getFileSystem().equals(that.getFileSystem())
                && toString().equals(that.toString());
        }
    }
}
