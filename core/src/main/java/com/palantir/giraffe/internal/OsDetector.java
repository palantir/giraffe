package com.palantir.giraffe.internal;

/**
 * Provides information about the current operating system.
 *
 * @author bkeyes
 */
public final class OsDetector {

    public static boolean isOsX() {
        // https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html
        return System.getProperty("os.name").contains("OS X");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private OsDetector() {
        throw new UnsupportedOperationException();
    }
}
