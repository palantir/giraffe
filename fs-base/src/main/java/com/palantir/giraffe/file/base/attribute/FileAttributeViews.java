package com.palantir.giraffe.file.base.attribute;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.attribute.FileAttributeView;

/**
 * Utilities for {@link FileAttributeView}s and their attribute types.
 *
 * @author bkeyes
 */
public final class FileAttributeViews {

    /**
     * The {@link java.nio.file.attribute.BasicFileAttributeView
     * BasicFileAttributeView} name.
     */
    public static final String BASIC_NAME = "basic";

    /**
     * The {@link java.nio.file.attribute.PosixFileAttributeView
     * PosixFileAttributeView} name.
     */
    public static final String POSIX_NAME = "posix";

    /**
     * The {@link java.nio.file.attribute.DosFileAttributeView
     * DosFileAttributeView} name.
     */
    public static final String DOS_NAME = "dos";

    /**
     * Converts a subclass of {@code targetType} into a true view of type
     * {@code targetType}. The behavior of the returned view is identical to the
     * input view except for the {@code name} method, which returns
     * {@code targetName}. If the view's name is already {@code targetName},
     * this method does nothing.
     *
     * @param view the view to convert
     * @param targetName the name of the target view
     * @param targetType the type of the target view, a subclass of
     *        {@link FileAttributeView}
     *
     * @return a version of the input view with the target type and name
     */
    public static <A extends FileAttributeView> A upcast(A view, String targetName,
            Class<A> targetType) {
        if (view.name().equals(targetName)) {
            return view;
        } else {
            @SuppressWarnings("unchecked")
            A proxy = (A) Proxy.newProxyInstance(targetType.getClassLoader(),
                    new Class<?>[] { targetType },
                    new NameChangingHandler(targetName, view));
            return proxy;
        }
    }

    private static final class NameChangingHandler implements InvocationHandler {
        private static final String NAME_METHOD_NAME = "name";

        private final FileAttributeView view;
        private final String name;

        private NameChangingHandler(String name, FileAttributeView view) {
            this.name = name;
            this.view = view;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals(NAME_METHOD_NAME)) {
                return name;
            } else {
                try {
                    return method.invoke(view, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }
    }

    private FileAttributeViews() {
        throw new UnsupportedOperationException();
    }
}
