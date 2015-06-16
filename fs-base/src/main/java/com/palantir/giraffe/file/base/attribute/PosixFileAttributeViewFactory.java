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
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;

/**
 * An abstract factory that creates {@code PosixFileAttributeView} and
 * {@code BasicFileAttributeView} instances. Both views are backed by an
 * implementation of {@link AnnotatedPosixFileAttributeView}.
 *
 * @author bkeyes
 */
public abstract class PosixFileAttributeViewFactory {

    public FileAttributeViewFactory<AnnotatedPosixFileAttributeView> getPosixFactory() {
        return new FileAttributeViewFactory<AnnotatedPosixFileAttributeView>() {
            @Override
            public String viewName() {
                return FileAttributeViews.POSIX_NAME;
            }

            @Override
            public Class<? extends FileAttributeView> viewType() {
                return PosixFileAttributeView.class;
            }

            @Override
            public Class<? extends BasicFileAttributes> attributesType() {
                return PosixFileAttributes.class;
            }

            @Override
            public AnnotatedPosixFileAttributeView newView(Path path, LinkOption[] options) {
                return createView(path, options);
            }
        };
    }

    public FileAttributeViewFactory<AnnotatedBasicFileAttributeView> getBasicFactory() {
        return new FileAttributeViewFactory<AnnotatedBasicFileAttributeView>() {
            @Override
            public String viewName() {
                return FileAttributeViews.BASIC_NAME;
            }

            @Override
            public Class<? extends FileAttributeView> viewType() {
                return BasicFileAttributeView.class;
            }

            @Override
            public Class<? extends BasicFileAttributes> attributesType() {
                return BasicFileAttributes.class;
            }

            @Override
            public AnnotatedBasicFileAttributeView newView(Path path, LinkOption[] options) {
                return FileAttributeViews.upcast(createView(path, options),
                        FileAttributeViews.BASIC_NAME,
                        AnnotatedBasicFileAttributeView.class);
            }
        };
    }

    protected abstract AnnotatedPosixFileAttributeView createView(Path path, LinkOption[] options);

}
