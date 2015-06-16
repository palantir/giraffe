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
package com.palantir.giraffe.command;

import java.io.IOException;

/**
 * Systems that implement this interface can be converted into
 * {@link ExecutionSystem} instances.
 *
 * @author bkeyes
 */
public interface ExecutionSystemConvertible {

    /**
     * Returns an open {@link ExecutionSystem} that accesses the same resources
     * as this system.
     * <p>
     * The returned system is independent from this system and either can be
     * closed without affecting the other.
     *
     * @throws IOException if an I/O error occurs while creating the new system
     */
    ExecutionSystem asExecutionSystem() throws IOException;

}
