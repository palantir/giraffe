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
