package com.palantir.giraffe.file.base.attribute;

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link PosixFileAttributes}.
 *
 * @author bkeyes
 */
public interface AnnotatedPosixFileAttributes extends PosixFileAttributes,
        AnnotatedBasicFileAttributes {

    @Override
    @Attribute
    UserPrincipal owner();

    @Override
    @Attribute
    GroupPrincipal group();

    @Override
    @Attribute
    Set<PosixFilePermission> permissions();

}
