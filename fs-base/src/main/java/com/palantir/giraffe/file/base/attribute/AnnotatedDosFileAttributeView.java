package com.palantir.giraffe.file.base.attribute;

import java.io.IOException;
import java.nio.file.attribute.DosFileAttributeView;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link DosFileAttributeView}.
 *
 * @author bkeyes
 */
public interface AnnotatedDosFileAttributeView extends DosFileAttributeView,
        AnnotatedBasicFileAttributeView {

    @Override
    AnnotatedDosFileAttributes readAttributes() throws IOException;

    @Override
    @Attribute("readonly")
    void setReadOnly(boolean value) throws IOException;

    @Override
    @Attribute
    void setHidden(boolean value) throws IOException;

    @Override
    @Attribute
    void setSystem(boolean value) throws IOException;

    @Override
    @Attribute
    void setArchive(boolean value) throws IOException;

}
