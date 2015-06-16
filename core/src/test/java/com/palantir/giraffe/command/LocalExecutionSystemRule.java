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

import java.nio.file.Path;

import org.junit.rules.ExternalResource;

import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Provides access to the local execution system system for testing.
 *
 * @author bkeyes
 */
public class LocalExecutionSystemRule extends ExternalResource implements ExecutionSystemTestRule {

    private final Path testFilesRoot;

    public LocalExecutionSystemRule(Path testFilesRoot) {
        this.testFilesRoot = testFilesRoot;
    }

    @Override
    public Path getTestFilesRoot() {
        return testFilesRoot;
    }

    @Override
    public Command.Builder getCommandBuilder(String executable) {
        return ExecutionSystems.getDefault().getCommandBuilder(executable);
    }

    @Override
    public String name() {
        return "local";
    }

}
