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
