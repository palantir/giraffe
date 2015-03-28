package com.palantir.giraffe.file.base.attribute;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

final class AttributeMethodExtractor {

    public static Map<String, Method> getters(Class<? extends AnnotatedFileAttributes> type) {
        return find(type, Extractor.GETTER);
    }

    public static Map<String, Method> setters(Class<? extends AnnotatedFileAttributeView> type) {
        return find(type, Extractor.SETTER);
    }

    private enum Extractor {
        GETTER {
            @Override
            boolean isExtractable(Method method) {
                if (method.isAnnotationPresent(Attribute.class)) {
                    return isGetter(method);
                }
                return false;
            }

            @Override
            String getAttributeName(Method method) {
                String name = method.getAnnotation(Attribute.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                return name;
            }
        },

        SETTER {
            @Override
            boolean isExtractable(Method method) {
                if (method.isAnnotationPresent(Attribute.class)) {
                    return isSetter(method);
                }
                return false;
            }

            @Override
            String getAttributeName(Method method) {
                String name = method.getAnnotation(Attribute.class).value();
                if (name.isEmpty()) {
                    name = stripSet(method);
                }
                return name;
            }

            private String stripSet(Method method) {
                String name = method.getName();
                checkArgument(!name.equals(SETTER_PREFIX),
                        "empty attribute name after processing %s",
                        method.getDeclaringClass().getName() + "." + method.getName());

                if (name.startsWith(SETTER_PREFIX)) {
                    name = name.substring(SETTER_PREFIX.length());
                    name = name.substring(0, 1).toLowerCase() + name.substring(1);
                }
                return name;
            }
        };

        abstract boolean isExtractable(Method method);
        abstract String getAttributeName(Method method);

        private static final String SETTER_PREFIX = "set";

        private static boolean isGetter(Method method) {
            return method.getParameterTypes().length == 0 && method.getReturnType() != Void.class;
        }

        private static boolean isSetter(Method method) {
            return method.getParameterTypes().length == 1;
        }
    }

    private static Map<String, Method> find(Class<?> type, Extractor extractor) {
        Map<String, Method> result = new HashMap<>();

        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        hierarchy.add(type);
        while (!hierarchy.isEmpty()) {
            Class<?> current = hierarchy.removeFirst();
            for (Method method : current.getDeclaredMethods()) {
                if (extractor.isExtractable(method)) {
                    String name = extractor.getAttributeName(method);
                    if (!result.containsKey(name)) {
                        result.put(name, method);
                    }
                }
            }

            if (current.getSuperclass() != null) {
                hierarchy.add(current.getSuperclass());
            }
            hierarchy.addAll(Arrays.asList(current.getInterfaces()));
        }

        return result;
    }

    private AttributeMethodExtractor() {
        throw new UnsupportedOperationException();
    }

}
