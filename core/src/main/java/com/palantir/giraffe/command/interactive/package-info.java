/**
 * Provides classes for performing interactive command execution.
 * Interactive implies that it is of interest to perform actions
 * before the command completes.
 * <p>
 * One use case is commands that require user input. This use case
 * is supported by using &ltprompt, response&gt pairs which are modeled
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

