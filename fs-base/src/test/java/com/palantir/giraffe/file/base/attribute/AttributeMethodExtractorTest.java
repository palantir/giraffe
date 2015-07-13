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

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;

/**
 * Tests basic functionality of {@link DynamicAttributeAccessor}.
 *
 * @author bkeyes
 */
public class AttributeMethodExtractorTest {

    @Test
    public void discoversDeclaredGetters() {
        Map<String, Method> attrs = AttributeMethodExtractor.getters(
                AnnotatedPosixFileAttributes.class);

        assertHasAttribute(attrs, "owner");
        assertHasAttribute(attrs, "group");
        assertHasAttribute(attrs, "permissions");
    }

    @Test
    public void discoversInheritedGetters() {
        Map<String, Method> attrs = AttributeMethodExtractor.getters(
                AnnotatedPosixFileAttributes.class);

        assertHasAttribute(attrs, "lastModifiedTime");
        assertHasAttribute(attrs, "lastAccessTime");
        assertHasAttribute(attrs, "creationTime");
        assertHasAttribute(attrs, "isRegularFile");
        assertHasAttribute(attrs, "isDirectory");
        assertHasAttribute(attrs, "isSymbolicLink");
        assertHasAttribute(attrs, "isOther");
        assertHasAttribute(attrs, "size");
        assertHasAttribute(attrs, "fileKey");
    }

    @Test
    public void discoversDeclaredSetters() {
        Map<String, Method> attrs = AttributeMethodExtractor.setters(
                AnnotatedDosFileAttributeView.class);

        assertHasAttribute(attrs, "readonly");
        assertHasAttribute(attrs, "system");
        assertHasAttribute(attrs, "archive");
        assertHasAttribute(attrs, "hidden");
    }

    @Test
    public void discoversInheritedSetters() {
        Map<String, Method> attrs = AttributeMethodExtractor.setters(
                AnnotatedDosFileAttributeView.class);

        assertHasAttribute(attrs, "lastModifiedTime");
        assertHasAttribute(attrs, "lastAccessTime");
        assertHasAttribute(attrs, "creationTime");
    }

    private static void assertHasAttribute(Map<String, ?> attrs, String attr) {
        assertThat("missing attribute", attrs, hasKey(attr));
    }

}
