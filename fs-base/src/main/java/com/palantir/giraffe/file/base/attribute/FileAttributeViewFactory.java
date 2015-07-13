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
