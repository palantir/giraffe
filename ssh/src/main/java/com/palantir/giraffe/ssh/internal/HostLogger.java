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
package com.palantir.giraffe.ssh.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.google.common.collect.ImmutableSet;
import com.palantir.giraffe.host.Host;

/**
 * An SLF4J-compatible logger that stores a specified host in the MDC before
 * each logging statement.
 *
 * @author bkeyes
 */
class HostLogger {

    private static final String MDC_HOST_KEY = "giraffe-ssh-host";

    public static Logger create(Logger delegate, Host host) {
        return (Logger) Proxy.newProxyInstance(HostLogger.class.getClassLoader(),
                new Class<?>[] { Logger.class },
                new HostMdcHandler(delegate, host));
    }

    private static final class HostMdcHandler implements InvocationHandler {

        private static final ImmutableSet<String> LOG_METHODS = ImmutableSet.of(
                "trace", "debug", "info", "warn", "error");

        private final Logger delegate;
        private final Host host;

        HostMdcHandler(Logger delegate, Host host) {
            this.delegate = delegate;
            this.host = host;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (LOG_METHODS.contains(method.getName())) {
                    return log(method, args);
                } else {
                    return method.invoke(delegate, args);
                }
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private Object log(Method method, Object[] args) throws Throwable {
            MDC.put(MDC_HOST_KEY, host.getHostname());
            try {
                return method.invoke(delegate, args);
            } finally {
                MDC.remove(MDC_HOST_KEY);
            }
        }
    }

    private HostLogger() {
        throw new UnsupportedOperationException();
    }
}
