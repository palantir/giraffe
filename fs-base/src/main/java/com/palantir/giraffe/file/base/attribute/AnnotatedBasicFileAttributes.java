package com.palantir.giraffe.file.base.attribute;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link BasicFileAttributes}.
 *
 * @author bkeyes
 */
public interface AnnotatedBasicFileAttributes extends BasicFileAttributes,
        AnnotatedFileAttributes {

    @Override
    @Attribute
    FileTime lastModifiedTime();

    @Override
    @Attribute
    FileTime lastAccessTime();

    @Override
    @Attribute
    FileTime creationTime();

    @Override
    @Attribute
    boolean isRegularFile();

    @Override
    @Attribute
    boolean isDirectory();

    @Override
    @Attribute
    boolean isSymbolicLink();

    @Override
    @Attribute
    boolean isOther();

    @Override
    @Attribute
    long size();

    @Override
    @Attribute
    Object fileKey();

}
