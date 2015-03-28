package com.palantir.giraffe.file.base.attribute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * A mapping from attribute identifiers to {@link FileAttributeViewFactory}s.
 * Factories may be retrived by view name, view type, or attribute type.
 *
 * @author bkeyes
 */
public final class FileAttributeViewRegistry {

    private static final String BASIC_NAME = "basic";

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link FileAttributeViewRegistry} objects.
     */
    public static final class Builder {

        private final Set<String> names = new HashSet<>();
        private final Set<FileAttributeViewFactory<?>> factories = new HashSet<>();

        private Builder() {
            // use static factory method
        }

        public Builder add(FileAttributeViewFactory<?> factory) {
            checkNotNull(factory, "factory must be non-null").viewName();

            String name = factory.viewName();
            checkArgument(names.add(name), "already registered factory for view '%s'", name);

            factories.add(factory);
            return this;
        }

        public FileAttributeViewRegistry build() {
            checkState(names.contains(BASIC_NAME), "'%s' is not registered", BASIC_NAME);
            return new FileAttributeViewRegistry(this);
        }
    }

    private final ImmutableSet<FileAttributeViewFactory<?>> factories;
    private final ImmutableSet<String> names;

    private FileAttributeViewRegistry(Builder builder) {
        this.factories = ImmutableSet.copyOf(builder.factories);
        this.names = ImmutableSet.copyOf(builder.names);
    }

    public FileAttributeViewFactory<?> getByViewName(String viewName) {
        for (FileAttributeViewFactory<?> factory : factories) {
            if (factory.viewName().equals(viewName)) {
                return factory;
            }
        }
        throw noFactoryError(viewName);
    }

    public FileAttributeViewFactory<?> getByViewType(Class<? extends FileAttributeView> type) {
        for (FileAttributeViewFactory<?> factory : factories) {
            if (factory.viewType().equals(type)) {
                return factory;
            }
        }
        throw noFactoryError(type.getName());
    }

    public FileAttributeViewFactory<?> getByAttributesType(
            Class<? extends BasicFileAttributes> type) {
        for (FileAttributeViewFactory<?> factory : factories) {
            if (factory.attributesType().equals(type)) {
                return factory;
            }
        }
        throw noFactoryError(type.getName());
    }

    private static UnsupportedOperationException noFactoryError(String name) {
        throw new UnsupportedOperationException("'" + name + "' attributes are not supported");
    }

    public Set<String> getRegisteredViews() {
        return names;
    }

}
