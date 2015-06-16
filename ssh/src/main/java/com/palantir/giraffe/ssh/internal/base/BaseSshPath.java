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
