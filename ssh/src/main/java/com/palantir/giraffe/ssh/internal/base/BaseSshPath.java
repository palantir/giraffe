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
package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.nio.file.LinkOption;

import javax.annotation.CheckForNull;

import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.file.base.AbstractImmutableListPath;
import com.palantir.giraffe.file.base.ImmutableListPathCore;

import net.schmizz.sshj.sftp.SFTPClient;

/**
 * Abstract {@code Path} implementation for SSH-based file systems.
 *
 * @author bkeyes
 *
 * @param <P> the type of the implementing class
 */
public abstract class BaseSshPath<P extends BaseSshPath<P>> extends AbstractImmutableListPath<P> {

    static final ImmutableListPathCore.Parser PARSER = ImmutableListPathCore.parser(
            BaseSshFileSystem.SEPARATOR);

    private final BaseSshFileSystem<P> fs;

    protected BaseSshPath(BaseSshFileSystem<P> fs, ImmutableListPathCore core) {
        super(core);
        this.fs = fs;
    }

    @Override
    public BaseSshFileSystem<P> getFileSystem() {
        return fs;
    }

    @CheckForNull
    public String getInode() throws IOException {
        CommandResult result = getFileSystem().execute("stat", "-c", "%i", this);
        if (result.getExitStatus() == 0) {
            return result.getStdOut().trim();
        } else {
            return null;
        }
    }

    @Override
    public P toRealPath(LinkOption... options) throws IOException {
        for (LinkOption option : options) {
            if (option == LinkOption.NOFOLLOW_LINKS) {
                throw new UnsupportedOperationException("NOFOLLOW_LINKS not supported");
            } else {
                throw new IllegalArgumentException("unknown option: " + option);
            }
        }

        try (SFTPClient sftp = getFileSystem().openSftpClient()) {
            String realPath = sftp.canonicalize(toString());
            return newPath(PARSER.parse(realPath));
        }
    }

}
