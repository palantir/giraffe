package com.palantir.giraffe.file;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.palantir.giraffe.file.base.ImmutableListPathCore;
import com.palantir.giraffe.file.base.ImmutableListPathCore.Parser.NamedRootFormat;

// @formatter:off
/**
 * A {@link Path}-like object that defines a file system independent path syntax
 * and is not associated with any {@code FileSystem} instance.
 * <p>
 * Uniform paths use the following UNIX-like syntax:
 * <blockquote>
 * {@code [[//root]/]path/to/file/or/directory}
 * </blockquote> where {@code []} indicates an optional component and {@code /}
 * serves as the path separator. All names are case-sensitive.
 * <p>
 * If the root component is present, the path is absolute. For single-root file
 * systems, the <i>root prefix</i> {@code //} and the root name are omitted and
 * the path starts with a single forward slash. For multi-root file system, the
 * path starts with the root prefix followed by the name of the root and a
 * single forward slash. Root names that contain the path separator ({@code /})
 * are not allowed.
 * <p>
 * {@code UniformPath}s can be converted to standard {@code Path}s using the
 * {@link #toPath(FileSystem) toPath} method.
 *
 * @author bkeyes
 */
// @formatter:on
public final class UniformPath implements Iterable<UniformPath>, Comparable<UniformPath> {

    private static final String SEPARATOR = "/";
    private static final String ROOT_PREFIX = "//";

    private static final ImmutableListPathCore.Parser PARSER =
            ImmutableListPathCore.namedRootParser(
                    SEPARATOR, NamedRootFormat.OPTIONAL_PREFIX, ROOT_PREFIX);

    /**
     * Converts a path string, or a sequence of strings that form a path string
     * when joined, to a {@code UniformPath}.
     *
     * @param first the path string or initial part of the path string
     * @param more additional strings to join to form the path string
     */
    public static UniformPath get(String first, String... more) {
        return get(Lists.asList(first, more));
    }

    /**
     * Converts a sequence of strings that form a path string when joined to a
     * {@code UniformPath}.
     *
     * @param components the components to join to form the path string
     */
    public static UniformPath get(Iterable<String> components) {
        return fromString(Joiner.on(SEPARATOR).join(components));
    }

    /**
     * Converts the specified {@link Path} into a {@code UniformPath}.
     * <p>
     * For any path {@code p}, the following relationship will always hold:
     *
     * <pre>
     * {@code UniformPath.fromPath(p).toPath(p.getFileSystem()).equals(p)}
     * </pre>
     *
     * @param path the path to convert
     *
     * @return a {@code UniformPath} that is equivalent to the given path
     *
     * @throws IllegalArgumentException if any path segment contains the uniform
     *         path separator
     */
    public static UniformPath fromPath(Path path) {
        checkNotNull(path, "path must be non-null");
        Iterable<String> segments = Iterables.transform(path, new Function<Path, String>() {
            @Override
            public String apply(Path input) {
                String seg = input.toString();
                checkArgument(!seg.contains(SEPARATOR), "segment '%s' contains separator", seg);
                return seg;
            }
        });
        return new UniformPath(new ImmutableListPathCore(getRootString(path), segments));
    }

    private static String getRootString(Path p) {
        Path root = p.getRoot();
        if (root == null) {
            return null;
        } else {
            String cleanRoot = cleanRoot(root.toString(), p.getFileSystem().getSeparator());
            return cleanRoot.isEmpty() ? "" : ROOT_PREFIX + cleanRoot;
        }
    }

    private static String cleanRoot(String root, String fsSeparator) {
        String cleaned = root;
        if (root.endsWith(fsSeparator)) {
            cleaned = root.substring(0, root.length() - fsSeparator.length());
        }
        checkArgument(!cleaned.contains(SEPARATOR), "root '%s' contains separator", root);
        return cleaned;
    }

    private static UniformPath fromString(String path) {
        return new UniformPath(PARSER.parse(path));
    }

    private final ImmutableListPathCore core;

    private UniformPath(ImmutableListPathCore core) {
        this.core = core;
    }

    public boolean isAbsolute() {
        return core.isAbsolute();
    }

    public UniformPath getRoot() {
        ImmutableListPathCore root = core.getRoot();
        if (root == null) {
            return null;
        } else {
            return new UniformPath(root);
        }
    }

    public UniformPath getFileName() {
        ImmutableListPathCore name = core.getFileName();
        if (name == null) {
            return null;
        } else {
            return new UniformPath(name);
        }
    }

    public UniformPath getParent() {
        ImmutableListPathCore parent = core.getParent();
        if (parent == null) {
            return null;
        } else {
            return new UniformPath(parent);
        }
    }

    public int getNameCount() {
        return core.getNameCount();
    }

    public UniformPath getName(int index) {
        return new UniformPath(core.getName(index));
    }

    public UniformPath subpath(int beginIndex, int endIndex) {
        return new UniformPath(core.subpath(beginIndex, endIndex));
    }

    public boolean startsWith(UniformPath other) {
        return core.startsWith(other.core);
    }

    public boolean startsWith(String other) {
        return startsWith(fromString(other));
    }

    public boolean endsWith(UniformPath other) {
        return core.endsWith(other.core);
    }

    public boolean endsWith(String other) {
        return endsWith(fromString(other));
    }

    public UniformPath normalize() {
        return new UniformPath(core.normalize());
    }

    public UniformPath resolve(UniformPath other) {
        return new UniformPath(core.resolve(other.core));
    }

    public UniformPath resolve(String other) {
        return resolve(fromString(other));
    }

    public UniformPath resolveSibling(UniformPath other) {
        return new UniformPath(core.resolveSibling(other.core));
    }

    public UniformPath resolveSibling(String other) {
        return resolveSibling(fromString(other));
    }

    public UniformPath relativize(UniformPath other) {
        return new UniformPath(core.relativize(other.core));
    }

    @Override
    public Iterator<UniformPath> iterator() {
        return new UnmodifiableIterator<UniformPath>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < getNameCount();
            }

            @Override
            public UniformPath next() {
                if (i < getNameCount()) {
                    UniformPath result = getName(i);
                    i++;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * Converts this uniform path to a real {@link Path} on the default file
     * system.
     *
     * @see #toPath(FileSystem)
     */
    public Path toPath() {
        return toPath(FileSystems.getDefault());
    }

    /**
     * Converts this uniform path to a real {@link Path} on the given file
     * system.
     * <p>
     * If this path is a relative path, each segment is passed as a separate
     * argument to the file system's
     * {@link FileSystem#getPath(String, String...) getPath} method. If this
     * path is an absolute path, a relative path is constructed as described
     * then resolved against a root path obtained using the following method:
     * <ol>
     * <li>Get all root directories on the file system</li>
     * <li>If this path does not have a named root <em>and</em> there is one
     * root, use that root</li>
     * <li>If there are multiple roots <em>or</em> this path has a named root,
     * compare this path's root to the string version of each file system root.
     * If this path's root equals a file system root, ignoring any trailing path
     * separator, use that root</li>
     * <li>If there are no matches, throw an {@code IllegalArgumentException}</li>
     * </ol>
     *
     * @param fs the {@link FileSystem} on which to create a path
     *
     * @throws IllegalArgumentException if this is an absolute path and the
     *         given file system does not have a matching root
     */
    public Path toPath(FileSystem fs) {
        checkNotNull(fs, "file system must be non-null");
        if (isAbsolute()) {
            return findRoot(fs, getRootName()).resolve(toRelativePath(fs));
        } else {
            return toRelativePath(fs);
        }
    }

    private Path findRoot(FileSystem fs, String target) {
        Iterable<Path> rootDirs = fs.getRootDirectories();
        if (isSingleRoot() && Iterables.size(rootDirs) == 1) {
            return Iterables.getOnlyElement(rootDirs);
        } else {
            for (Path dir : rootDirs) {
                String root = dir.toString();
                if (root.equals(target) || root.equals(target + fs.getSeparator())) {
                    return dir;
                }
            }
        }
        throw new IllegalArgumentException("no root directory matching '" + target + "'");
    }

    private Path toRelativePath(FileSystem fs) {
        ImmutableList<String> segments = core.getPathSegments();
        if (segments.isEmpty()) {
            return fs.getPath("");
        } else {
            String first = segments.get(0);
            String[] more = segments.subList(1, segments.size()).toArray(new String[0]);
            return fs.getPath(first, more);
        }
    }

    private boolean isSingleRoot() {
        return isAbsolute() && core.getRootString().isEmpty();
    }

    private String getRootName() {
        String rootString = core.getRootString();
        if (rootString.startsWith(ROOT_PREFIX)) {
            return rootString.substring(ROOT_PREFIX.length());
        } else {
            return rootString;
        }
    }

    @Override
    public int compareTo(UniformPath other) {
        return core.compareTo(other.core);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof UniformPath)) {
            return false;
        } else {
            return core.equals(((UniformPath) obj).core);
        }
    }

    @Override
    public int hashCode() {
        return core.hashCode();
    }

    @Override
    public String toString() {
        return core.toPathString(SEPARATOR);
    }

}
