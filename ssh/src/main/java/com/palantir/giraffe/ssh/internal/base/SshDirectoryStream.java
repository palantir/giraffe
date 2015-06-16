package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.palantir.giraffe.file.base.AbstractFilteredDirectoryStream;

import net.schmizz.sshj.sftp.RemoteResourceInfo;

final class SshDirectoryStream extends AbstractFilteredDirectoryStream<Path> {

    private static final Predicate<RemoteResourceInfo> META_FILTER =
            new Predicate<RemoteResourceInfo>() {
                @Override
                public boolean apply(RemoteResourceInfo input) {
                    String filename = input.getName();
                    return !filename.equals(".") && !filename.equals("..");
                }
            };

    private final BaseSshPath<?> dir;
    private final Iterator<RemoteResourceInfo> entryIterator;

    SshDirectoryStream(BaseSshPath<?> dir,
                       Iterable<RemoteResourceInfo> entries,
                       Filter<? super Path> filter) {
        super(filter);
        this.dir = dir;
        entryIterator = Iterators.filter(entries.iterator(), META_FILTER);
    }

    @Override
    protected void doClose() throws IOException {
        dir.getFileSystem().logger().debug("closing directory stream for {}", dir);
    }

    @Override
    protected Iterator<Path> entryIterator() {
        return Iterators.transform(entryIterator, new Function<RemoteResourceInfo, Path>() {
            @Override
            public Path apply(RemoteResourceInfo input) {
                return dir.resolve(input.getName());
            }
        });
    }

}
