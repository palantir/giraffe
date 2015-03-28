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
