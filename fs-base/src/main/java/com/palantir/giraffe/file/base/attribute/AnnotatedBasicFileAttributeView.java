package com.palantir.giraffe.file.base.attribute;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link BasicFileAttributeView}.
 *
 * @author bkeyes
 */
public interface AnnotatedBasicFileAttributeView extends BasicFileAttributeView,
        AnnotatedFileAttributeView {

    @Override
    AnnotatedBasicFileAttributes readAttributes() throws IOException;

    @Attribute
    void setLastModifiedTime(FileTime time) throws IOException;

    @Attribute
    void setLastAccessTime(FileTime time) throws IOException;

    @Attribute
    void setCreationTime(FileTime time) throws IOException;

}
