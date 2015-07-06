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
package com.palantir.giraffe.host;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains information needed to open a {@link HostControlSystem}.
 * <p>
 * Subclasses may add additional methods to provide easier access to values in
 * the options map. However, subclasses must store all values in this map to
 * preserve the contracts of {@code equals} and {@code hashCode}.
 *
 * @author bkeyes
 */
public class SystemRequest {

    private final URI uri;
    private final Map<String, Object> options;

    public SystemRequest(URI uri) {
        this.uri = checkNotNull(uri, "uri must be non-null");
        this.options = new HashMap<>();
    }

    public final URI uri() {
        return uri;
    }

    public final Object get(String key) {
        return options.get(key);
    }

    public final <T> T get(String key, Class<? extends T> type) {
        Object value = options().get(key);
        checkState(value != null, "no value for option '%s'", key);
        checkState(type.isInstance(value),
                "option '%s' does not have type %s [actual: %s]",
                key,
                type.getName(),
                value.getClass().getName());

        return type.cast(value);
    }

    public final void set(String key, Object value) {
        options.put(key, value);
    }

    public final void setAll(Map<String, ?> newOptions) {
        options.putAll(newOptions);
    }

    public final boolean contains(String key) {
        return options.containsKey(key);
    }

    public final Map<String, Object> options() {
        return options;
    }

    @Override
    public final int hashCode() {
        int result = 1;
        result = 31 * result + uri.hashCode();
        result = 31 * result + options.hashCode();
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SystemRequest)) {
            return false;
        } else {
            SystemRequest that = (SystemRequest) obj;
            return uri.equals(that.uri) && options.equals(that.options);
        }
    }


}
