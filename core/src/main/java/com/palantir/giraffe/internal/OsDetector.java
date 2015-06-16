/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
