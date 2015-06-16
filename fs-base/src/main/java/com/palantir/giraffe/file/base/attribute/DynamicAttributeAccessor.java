package com.palantir.giraffe.file.base.attribute;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

/**
 * Reads annotations on {@link AnnotatedFileAttributeView}s to provide dynamic
 * access to file attributes.
 *
 * @author bkeyes
 */
public final class DynamicAttributeAccessor {

    private final AnnotatedFileAttributeView view;

    public DynamicAttributeAccessor(AnnotatedFileAttributeView view) {
        this.view = view;
    }

    public Map<String, Object> readAttributes(String attributes) throws IOException {
        ImmutableSet<String> attributeSet = ImmutableSet.copyOf(attributes.split(","));
        if (attributeSet.contains("*")) {
            return readAllAttributes();
        } else {
            return readAttributes(attributeSet);
        }
    }

    public Map<String, Object> readAllAttributes() throws IOException {
        AnnotatedFileAttributes attributes = view.readAttributes();
        Map<String, Method> getters = AttributeMethodExtractor.getters(attributes.getClass());

        Map<String, Object> result = new HashMap<>();
        for (Entry<String, Method> e : getters.entrySet()) {
            result.put(e.getKey(), invoke(e.getValue(), attributes));
        }
        return result;
    }

    public Map<String, Object> readAttributes(Set<String> names) throws IOException {
        AnnotatedFileAttributes attributes = view.readAttributes();
        Map<String, Method> getters = AttributeMethodExtractor.getters(attributes.getClass());

        Map<String, Object> result = new HashMap<>();
        for (String attribute : names) {
            Method getter = getters.get(attribute);
            if (getter != null) {
                result.put(attribute, invoke(getter, attributes));
            } else {
                throw new IllegalArgumentException("unrecognized attribute: '" + attribute + "'");
            }
        }
        return result;
    }

    public void setAttribute(String name, Object value) throws IOException {
        Map<String, Method> setters = AttributeMethodExtractor.setters(view.getClass());

        Method setter = setters.get(name);
        if (setter != null) {
            invoke(setter, view, value);
        } else {
            throw new IllegalArgumentException("unrecognized attribute: '" + name + "'");
        }
    }

    private static Object invoke(Method m, Object obj, Object... args) throws IOException {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(methodString(m) + " is not accessible", e);
        } catch (InvocationTargetException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
            throw new IllegalStateException("error invoking " + methodString(m), e.getCause());
        }
    }

    private static String methodString(Method m) {
        return m.getDeclaringClass().getName() + "." + m.getName();
    }
}
