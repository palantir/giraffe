package com.palantir.giraffe.file.base.attribute;

/**
 * A marker interface that indicates the implementing class supports
 * annotation-based dynamic file attribute access.
 * <p>
 * Methods that return file attributes should be annotated with
 * {@link Attribute}.
 *
 * @author bkeyes
 */
public interface AnnotatedFileAttributes {

}
