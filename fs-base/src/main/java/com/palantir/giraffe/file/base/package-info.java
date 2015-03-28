/**
 * Provides utilities to make implementing custom file systems easier.
 * <p>
 * The JDK, despite requiring file system implementations to support certain
 * behaviors, provides very few public utility classes that implement these
 * behaviors. This packages provides some of these missing utilities, including
 * a basic {@link java.nio.file.Path} implementation, glob-to-regex conversion,
 * option Enum parsers, and more useful base classes for {@code FileSystem} and
 * {@code FileSystemProvider} implementations.
 */
package com.palantir.giraffe.file.base;
