package com.palantir.giraffe.ssh.internal.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with environment maps when creating SSH file and
 * execution systems.
 *
 * @author bkeyes
 */
public class SshEnvironments {

    private static final String DEFAULT_LOGGER_NAME = "com.palantir.giraffe.ssh";

    private static final String HOST_KEY = "ssh-host";
    private static final String CLIENT_KEY = "ssh-client";
    private static final String LOGGER_KEY = "logger";

    public static Map<String, ?> makeEnv(BaseSshHostAccessor<?> host) {
        Map<String, Object> env = new HashMap<>();
        env.put(HOST_KEY, checkNotNull(host));
        return env;
    }

    public static Map<String, ?> makeEnv(SharedSshClient client) {
        Map<String, Object> env = new HashMap<>();
        env.put(CLIENT_KEY, checkNotNull(client));
        return env;
    }

    public static SharedSshClient getClient(Map<String, ?> env, SshConnectionFactory conFactory)
            throws IOException {
        checkArgument(env.containsKey(HOST_KEY) || env.containsKey(CLIENT_KEY),
                "env must define one of %s or %s", HOST_KEY, CLIENT_KEY);

        if (env.containsKey(CLIENT_KEY)) {
            return get(CLIENT_KEY, SharedSshClient.class, env);
        } else {
            BaseSshHostAccessor<?> host = get(HOST_KEY, BaseSshHostAccessor.class, env);
            return new SharedSshClient(conFactory.newAuthedConnection(host));
        }
    }

    public static Logger getLogger(Map<String, ?> env) {
        if (env.containsKey(LOGGER_KEY)) {
            return get(LOGGER_KEY, Logger.class, env);
        } else {
            return LoggerFactory.getLogger(DEFAULT_LOGGER_NAME);
        }
    }

    private static <T> T get(String key, Class<T> type, Map<String, ?> env) {
        Object value = env.get(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format("'%s' is missing", key));
        } else if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(String.format("'%s' is not of type %s [%s]",
                    key, type.getName(), value.getClass().getName()));
        } else {
            return type.cast(value);
        }
    }

    private SshEnvironments() {
        throw new UnsupportedOperationException();
    }
}
