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
