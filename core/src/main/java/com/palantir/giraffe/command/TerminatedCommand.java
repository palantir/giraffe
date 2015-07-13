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
 * A simple container for information about terminated commands. Includes the
 * command, the context, and the result.
 *
 * @author bkeyes
 */
public class TerminatedCommand {

    public final Command command;
    public final CommandContext context;
    public final CommandResult result;

    public TerminatedCommand(Command command, CommandContext context, CommandResult result) {
        this.command = command;
        this.context = context;
        this.result = result;
    }
}
