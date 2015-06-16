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
