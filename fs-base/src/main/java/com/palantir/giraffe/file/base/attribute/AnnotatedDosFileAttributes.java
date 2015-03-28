package com.palantir.giraffe.file.base.attribute;

import java.nio.file.attribute.DosFileAttributes;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link DosFileAttributes}.
 *
 * @author bkeyes
 */
public interface AnnotatedDosFileAttributes extends DosFileAttributes,
        AnnotatedBasicFileAttributes {

    @Override
    @Attribute("readonly")
    boolean isReadOnly();

    @Override
    @Attribute("hidden")
    boolean isHidden();

    @Override
    @Attribute("archive")
    boolean isArchive();

    @Override
    @Attribute("system")
    boolean isSystem();

}
