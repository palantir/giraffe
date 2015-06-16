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

/**
 * Runtime exception thrown when an execution system cannot be found.
 *
 * @author bkeyes
 */
public final class ExecutionSystemNotFoundException extends RuntimeException {

    public ExecutionSystemNotFoundException() {}

    public ExecutionSystemNotFoundException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -378596008898522613L;
}
