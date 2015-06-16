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
