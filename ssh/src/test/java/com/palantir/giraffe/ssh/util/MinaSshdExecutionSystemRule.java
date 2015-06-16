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
package com.palantir.giraffe.ssh.util;

import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;

/**
 * Starts and stops an embedded SSH server for execution system tests.
 *
 * @author bkeyes
 */
public class MinaSshdExecutionSystemRule extends MinaSshdSystemRule
        implements ExecutionSystemTestRule {

    public MinaSshdExecutionSystemRule(Path workingDir) {
        super(workingDir);
    }

    @Override
    public Command.Builder getCommandBuilder(String executable) {
        return getHostControlSystem().getExecutionSystem().getCommandBuilder(executable);
    }

}
