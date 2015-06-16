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
package com.palantir.giraffe.file.base.attribute;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link BasicFileAttributes}.
 *
 * @author bkeyes
 */
public interface AnnotatedBasicFileAttributes extends BasicFileAttributes,
        AnnotatedFileAttributes {

    @Override
    @Attribute
    FileTime lastModifiedTime();

    @Override
    @Attribute
    FileTime lastAccessTime();

    @Override
    @Attribute
    FileTime creationTime();

    @Override
    @Attribute
    boolean isRegularFile();

    @Override
    @Attribute
    boolean isDirectory();

    @Override
    @Attribute
    boolean isSymbolicLink();

    @Override
    @Attribute
    boolean isOther();

    @Override
    @Attribute
    long size();

    @Override
    @Attribute
    Object fileKey();

}
