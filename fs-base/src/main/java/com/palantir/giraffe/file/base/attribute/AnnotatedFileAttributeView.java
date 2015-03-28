package com.palantir.giraffe.file.base.attribute;

import java.io.IOException;
import java.nio.file.attribute.FileAttributeView;

/**
 * A {@link FileAttributeView} that supports annotation-based dynamic file
 * attribute access.
 * <p>
 * Methods that set file attributes should be annotated with
 * {@link Attribute}.
 *
 * @author bkeyes
 */
public interface AnnotatedFileAttributeView extends FileAttributeView {

    AnnotatedFileAttributes readAttributes() throws IOException;

}
