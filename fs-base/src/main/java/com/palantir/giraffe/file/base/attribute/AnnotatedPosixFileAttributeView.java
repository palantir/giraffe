package com.palantir.giraffe.file.base.attribute;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link PosixFileAttributeView}.
 *
 * @author bkeyes
 */
public interface AnnotatedPosixFileAttributeView extends PosixFileAttributeView,
        AnnotatedBasicFileAttributeView {

    @Override
    AnnotatedPosixFileAttributes readAttributes() throws IOException;

    @Override
    @Attribute
    void setPermissions(Set<PosixFilePermission> perms) throws IOException;

    // TODO(bkeyes): handle FileOwnerAttributeView more generally?

    @Override
    @Attribute
    void setOwner(UserPrincipal owner) throws IOException;

    @Override
    @Attribute
    void setGroup(GroupPrincipal group) throws IOException;
}
