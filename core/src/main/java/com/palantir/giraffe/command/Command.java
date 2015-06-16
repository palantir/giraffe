package com.palantir.giraffe.command;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Represents an executable path and set of arguments that may be run by an
 * execution system.
 * <p>
 * Each {@code Command} is associated with an {@link ExecutionSystem} that can
 * execute it and implements system-dependent behavior, like argument escaping.
 * <p>
 * Implementations of this interface are immutable.
 *
 * @author pchen
 * @author bkeyes
 */
public interface Command {

    /**
     * Builds {@link Command} objects. For users, the builder is a convenient
     * way to add arguments dynamically or provide a base set of arguments that
     * can be extended by other code. Builders can be reused to create multiple
     * {@code Command} objects.
     *
     * <h3><a name="arg-handling">Argument Handling</a></h3>
     * <p>
     * Each argument is converted to a string by calling {@code toString} or
     * using the string {@code "null"} if the argument is {@code null}.
     * <p>
     * Escaping arguments is not required and may lead to unexpected behavior.
     * Any system-dependent escaping is handled by the execution system when the
     * resulting command is executed.
     */
    interface Builder {

        // Include both addArgument and addArguments to avoid overload ambiguity
        // between addArguments(Object, Object...) and addArguments(Iterable<?>)

        /**
         * Adds an argument to this command.
         *
         * @param arg the argument to add
         *
         * @return this builder
         *
         * @see <a href="#arg-handling">Argument Handling</a>
         */
        Builder addArgument(Object arg);

        /**
         * Adds arguments to this command.
         *
         * @param first the first argument to add
         * @param second the second argument to add
         * @param more any additional arguments to add
         *
         * @return this builder
         *
         * @see <a href="#arg-handling">Argument Handling</a>
         */
        Builder addArguments(Object first, Object second, Object... more);

        /**
         * Adds arguments to this command.
         *
         * @param args the arguments to add
         *
         * @return this builder
         *
         * @see <a href="#arg-handling">Argument Handling</a>
         */
        Builder addArguments(List<?> args);

        /**
         * Builds a new {@code Command} using the settings configured by this
         * builder. The builder may be reused to create more commands after
         * calling this method.
         */
        Command build();
    }

    /**
     * Returns the {@code ExecutionSystem} associated with this command.
     */
    ExecutionSystem getExecutionSystem();

    /**
     * Returns this command's executable. The value may be a string
     * representation of a path or a simple name.
     */
    String getExecutable();

    /**
     * Returns the arguments of this command.
     * <p>
     * Each element in the list corresponds to a single logical argument passed
     * to this command's executable. The arguments are the unmodified string
     * representations of the objects provided when this command was
     * constructed. The execution system may modify these arguments to escape
     * system-dependent special characters when this command is executed.
     */
    ImmutableList<String> getArguments();

    /**
     * Returns a human-readable string representation of this command. The
     * string is similar to the character sequence entered in a terminal to
     * manually execute this command, but may leave out necessary escape
     * characters.
     */
    @Override
    String toString();

    /**
     * Tests this {@code Command} for equality with another object.
     * <p>
     * If the other object is not a {@code Command} or is a {@code Command}
     * associated with a different execution system, this method returns
     * {@code false}.
     * <p>
     * Otherwise, two commands are equal if they have the same executable and
     * the same arguments.
     * <p>
     * This method satisfies the general contract of the
     * {@link java.lang.Object#equals(Object) Object.equals} method.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a hash code for this {@code Command}.
     * <p>
     * The hash code combines the hash codes of the executable and the arguments
     * list.
     * <p>
     * This method satisfies the general contract of the
     * {@link java.lang.Object#hashCode() Object.hashCode} method.
     */
    @Override
    int hashCode();
}
