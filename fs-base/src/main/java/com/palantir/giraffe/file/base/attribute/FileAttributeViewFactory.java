package com.palantir.giraffe.file.base.attribute;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;

/**
 * Creates {@link AnnotatedFileAttributeView}s for {@link Paths}.
 *
 * @author bkeyes
 *
 * @param <V> the type of view this factory creates
 */
public interface FileAttributeViewFactory<V extends AnnotatedFileAttributeView> {

    /**
     * Returns the name of views created by this factory.
     */
    String viewName();

    /**
     * Returns the base type of views created by this factory.
     */
    Class<? extends FileAttributeView> viewType();

    /**
     * Returns the type of attributes read by views created by this factory.
     */
    Class<? extends BasicFileAttributes> attributesType();

    /**
     * Creates a new view for the given path.
     *
     * @param path the {@link Path}
     * @param options indicates how symbolic links are handled
     */
    V newView(Path path, LinkOption[] options);
}
