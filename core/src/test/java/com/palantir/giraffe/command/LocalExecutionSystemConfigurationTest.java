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

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.palantir.giraffe.command.test.ExecutionSystemBaseTest;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Tests custom local execution system configuration.
 *
 * @author bkeyes
 */
public class LocalExecutionSystemConfigurationTest extends ExecutionSystemBaseTest {

    private static final String ENV_WHITELIST = "giraffe.command.local.envWhitelist";

    public LocalExecutionSystemConfigurationTest(ExecutionSystemTestRule esRule) {
        super(esRule);
        if (!(esRule instanceof LocalExecutionSystemRule)) {
            throw new IllegalArgumentException("Test requires LocalExecutionSystemRule");
        }
    }

    @Test
    public void filtersEnvironment() throws IOException {
        Map<String, String> defaultEnv = System.getenv();

        assertThat(defaultEnv, hasKey("PATH"));
        String path = defaultEnv.get("PATH");

        assertThat(defaultEnv, hasKey("HOME"));
        String home = defaultEnv.get("HOME");

        System.setProperty(ENV_WHITELIST, "PATH,HOME");
        try {
            Map<String, String> expected = ImmutableMap.of("PATH", path, "HOME", home);
            assertEquals("environment is incorrect", expected, readEnv());
        } finally {
            System.clearProperty(ENV_WHITELIST);
        }
    }

    private Map<String, String> readEnv() throws IOException {
        String stdout = Commands.execute(getSystemCommand("printenv")).getStdOut().trim();

        Map<String, String> env = new HashMap<>();
        for (String var : stdout.split("(\r|\n)+")) {
            if (!var.isEmpty()) {
                String[] parts = var.split("=", 2);
                env.put(parts[0], parts[1]);
            }
        }
        return env;
    }

    @Test
    public void emptyEnvironment() throws IOException {
        Map<String, String> defaultEnv = System.getenv();
        assertFalse("default environment is empty", defaultEnv.isEmpty());

        System.setProperty(ENV_WHITELIST, "");
        try {
            Map<String, String> env = readEnv();
            assertTrue("environment is not empty: " + env, env.isEmpty());
        } finally {
            System.clearProperty(ENV_WHITELIST);
        }
    }

}
