package com.palantir.giraffe.file.base;

/**
 * Defines basic functionality common to {@link java.nio.file.Path Path}-like
 * objects. Implementations of this class may be used to create real path
 * objects through composition.
 *
 * @author bkeyes
 *
 * @param <P> the type of the implementing class
 */
public interface PathCore<P extends PathCore<P>> extends Comparable<P> {

    boolean isAbsolute();

    P getRoot();

    P getFileName();

    P getParent();

    int getNameCount();

    P getName(int index);

    P subpath(int beginIndex, int endIndex);

    boolean startsWith(P other);

    boolean endsWith(P other);

    P normalize();

    P resolve(P other);

    P resolveSibling(P other);

    P relativize(P other);

    String toPathString(String separator);

}
