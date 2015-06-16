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
package com.palantir.giraffe.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import com.palantir.giraffe.file.base.SuppressedCloseable;

/**
 * An abstract base implementation of {@link HandlableProcess} that implements
 * stream closing.
 *
 * @author bkeyes
 */
public abstract class AbstractHandlableProcess implements HandlableProcess {

    @Override
    public final void closeStreams() throws IOException {
        closeSuppressed(getOutput(), getError(), getInput());
    }

    /**
     * Closes the given {@code Closeable}s in order, suppressing intermediate
     * exceptions and throwing the most recent exception thrown by a
     * {@code close()} method, if any.
     */
    protected static void closeSuppressed(Closeable... closeables) throws IOException {
        SuppressedCloseable.create(Arrays.asList(closeables)).close();
    }

}
