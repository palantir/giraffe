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

import java.io.IOException;

/**
 * Checked exception thrown when authentication fails.
 *
 * @author bkeyes
 */
public class AuthenticationException extends IOException {

    /**
     * Creates an exception with the specified hostname and detail message.
     *
     * @param hostname the hostname of the host on which authentication failed
     * @param message the detail message
     */
    public AuthenticationException(String hostname, String message) {
        this(hostname, message, null);
    }

    /**
     * Creates an exception with the specified hostname, detail message, and
     * cause.
     *
     * @param hostname the hostname of the host on which authentication failed
     * @param message the detail message
     * @param cause the cause of the authentication failure
     */
    public AuthenticationException(String hostname, String message, Throwable cause) {
        super("failed to authenticate to " + hostname + ": " + message, cause);
    }

    private static final long serialVersionUID = 6184687007850310201L;
}
