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

/**
 * Unchecked exception thrown when a hostname cannot be resolved to an
 * {@link java.net.InetAddress}.
 *
 * @author bkeyes
 */
public final class InetAddressUnresolvableException extends RuntimeException {

    private final String hostname;

    /**
     * Creates an exception with the specified hostname and cause.
     *
     * @param hostname the hostname that could not be resolved
     * @param cause the cause
     */
    public InetAddressUnresolvableException(String hostname, Throwable cause) {
        super(hostname + " could not be resolved", cause);
        this.hostname = hostname;
    }

    /**
     * Returns the hostname that could not be resolved.
     */
    public String getHostname() {
        return hostname;
    }

    private static final long serialVersionUID = 1L;
}
