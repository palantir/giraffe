package com.palantir.giraffe.command.test;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.CommandContext;
import com.palantir.giraffe.command.CommandEnvironment;
import com.palantir.giraffe.command.Commands;
import com.palantir.giraffe.command.test.creator.CommandContextCreator;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Tests that command execution respects the context.
 *
 * @author bkeyes
 */
public class ExecutionSystemContextTest extends ExecutionSystemBaseTest {

    public ExecutionSystemContextTest(ExecutionSystemTestRule esRule) {
        super(esRule);
    }

    @Test
    public void changesWorkingDirectory() throws Exception {
        Path dir = getExecutionSystemRule().getTestFilesRoot().resolve(CommandContextCreator.DIR);

        Command pwd = getSystemCommand("pwd");
        CommandContext context = CommandContext.workingDirectory(dir);
        String actualDir = Commands.execute(pwd, context, 10, TimeUnit.SECONDS).getStdOut().trim();
        assertEquals("incorrect directory", dir.toAbsolutePath().toString(), actualDir);
    }

    @Test
    public void emptyEnvironment() throws Exception {
        CommandEnvironment env = CommandEnvironment.emptyEnvironment();

        Map<String, String> actualEnv = readEnvironment(CommandContext.withEnvironment(env));
        assertEquals("incorrect environment", Collections.emptyMap(), actualEnv);
    }

    @Test
    public void emptyEnvironmentWithChanges() throws Exception {
        Map<String, String> changes = new HashMap<>();
        changes.put("HELLO", "WORLD");
        changes.put("FOO", "BAR");

        CommandEnvironment env = CommandEnvironment.emptyEnvironment();
        env.setAll(changes);

        Map<String, String> actualEnv = readEnvironment(CommandContext.withEnvironment(env));
        assertEquals("incorrect environment", changes, actualEnv);
    }

    @Test
    public void defaultEnvironmentWithChanges() throws Exception {
        Map<String, String> changes = new HashMap<>();
        changes.put("HOME", "/giraffe/barn");
        changes.put("FOO", "BAR");

        CommandEnvironment env = CommandEnvironment.defaultEnvironment();
        env.setAll(changes);

        Map<String, String> actualEnv = readEnvironment(CommandContext.withEnvironment(env));
        for (Entry<String, String> e : changes.entrySet()) {
            assertThat("incorrect key/value pair", actualEnv, hasEntry(e.getKey(), e.getValue()));
        }
    }

    @Test
    public void environmentValuesWithSpacesAndQuotes() throws Exception {
        Map<String, String> changes = new HashMap<>();
        changes.put("NAME", "Hugh G. Raffe");
        changes.put("FOO", "bar \"baz\"");

        CommandEnvironment env = CommandEnvironment.defaultEnvironment();
        env.setAll(changes);

        Map<String, String> actualEnv = readEnvironment(CommandContext.withEnvironment(env));
        for (Entry<String, String> e : changes.entrySet()) {
            assertThat("incorrect key/value pair", actualEnv, hasEntry(e.getKey(), e.getValue()));
        }
    }

    private Map<String, String> readEnvironment(CommandContext context) throws Exception {
        Command cmd = getSystemCommand("/usr/bin/env");
        String output = Commands.execute(cmd, context, 10, TimeUnit.SECONDS).getStdOut();

        Map<String, String> env = new HashMap<>();
        for (String line : output.split("(\n|\r\n)")) {
            if (!line.isEmpty()) {
                String[] pair = line.split("=", 2);
                env.put(pair[0], pair[1]);
            }
        }
        return env;
    }

}
