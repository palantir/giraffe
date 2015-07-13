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

import java.io.IOException;
import java.nio.file.attribute.DosFileAttributeView;

/**
 * An {@linkplain AnnotatedFileAttributes annotated} version of
 * {@link DosFileAttributeView}.
 *
 * @author bkeyes
 */
public interface AnnotatedDosFileAttributeView extends DosFileAttributeView,
        AnnotatedBasicFileAttributeView {

    @Override
    AnnotatedDosFileAttributes readAttributes() throws IOException;

    @Override
    @Attribute("readonly")
    void setReadOnly(boolean value) throws IOException;

    @Override
    @Attribute
    void setHidden(boolean value) throws IOException;

    @Override
    @Attribute
    void setSystem(boolean value) throws IOException;

    @Override
    @Attribute
    void setArchive(boolean value) throws IOException;

}
