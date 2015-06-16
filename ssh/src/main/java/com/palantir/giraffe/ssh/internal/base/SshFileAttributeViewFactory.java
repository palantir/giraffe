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

import java.nio.file.LinkOption;
import java.nio.file.Path;

import com.palantir.giraffe.file.base.LinkOptions;
import com.palantir.giraffe.file.base.attribute.AnnotatedPosixFileAttributeView;
import com.palantir.giraffe.file.base.attribute.PosixFileAttributeViewFactory;

final class SshFileAttributeViewFactory extends PosixFileAttributeViewFactory {

    private final BaseSshFileSystemProvider<?> provider;

    public SshFileAttributeViewFactory(BaseSshFileSystemProvider<?> provider) {
        this.provider = provider;
    }

    @Override
    protected AnnotatedPosixFileAttributeView createView(Path path, LinkOption[] options) {
        boolean followLinks = LinkOptions.followLinks(options);
        return new SshPosixFileAttributeView(provider.checkPath(path), followLinks);
    }
}
