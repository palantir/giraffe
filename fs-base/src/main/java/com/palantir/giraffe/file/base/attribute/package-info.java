/**
 * Provides annotation-based dynamic access to file attributes.
 * <p>
 * This package defines annotations that allow file system implementations to
 * easily implement dynamic attribute access on top of standard
 * {@linkplain java.nio.file.attribute.BasicFileAttributes attribute} and
 * {@linkplain java.nio.file.attribute.FileAttributeView view} objects. By
 * implementing the annotated versions of these interfaces instead of the base
 * versions provided by the JDK, clients can use
 * {@link com.palantir.giraffe.file.base.attribute.DynamicAttributeAccessor}
 * to implement the dynamic attribute access methods defined by
 * {@link java.nio.file.spi.FileSystemProvider}.
 */
package com.palantir.giraffe.file.base.attribute;
