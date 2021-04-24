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
/**
 * Provides classes for performing interactive command execution.
 * Interactive implies that it is of interest to perform actions
 * before the command completes.
 * <p>
 * One use case is commands that require user input. This use case
 * is supported by using &lt;prompt, response&gt; pairs which are modeled
 * by the {@link com.palantir.giraffe.command.interactive.AbstractResponseProvider}
 * object parameterized with {@link java.lang.String}.
 * This object is used by a {@link com.palantir.giraffe.command.interactive.ShellConversation}
 * to carry out a scripted conversation.
 * <p>
 * A second use case is long-running commands where it is of interest to
 * perform certain actions when certain output appears. A
 * {@link com.palantir.giraffe.command.interactive.CommandOutputTrigger}
 * is used to do this and will execute given {@link java.lang.Runnable}s on
 * string matches. Similar {@code ShellConversation}, this uses an
 * {@code ResponseProvider} but parameterized with {@link java.lang.Runnable}.
 * <p>
 * In all cases, strings can be matched via regular expression, exact
 * matching, or using a custom {@link com.google.common.base.Predicate}.
 */
package com.palantir.giraffe.command.interactive;

