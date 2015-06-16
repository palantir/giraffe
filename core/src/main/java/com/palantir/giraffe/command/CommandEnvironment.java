package com.palantir.giraffe.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the environment variables that are present when a {@link Command}
 * executes.
 * <p>
 * The environment is represented as a base and a set of additive changes. This
 * makes it simple to add and change values or reset the environment to a known
 * state. To remove or unset a variable, use a system-dependent method to read
 * the current environment as a map, remove the necessary variables, then set
 * the modified environment on top of an empty base.
 *
 * @author jchien
 */
public final class CommandEnvironment {

    /**
     * Defines the target of environment changes.
     */
    public enum BaseEnvironment {

        /**
         * The default environment. New variables with the same names as existing
         * variables will overwrite the previous values.
         */
        DEFAULT,

        /**
         * The empty environment. New variables are the only things defined in
         * the environment.
         */
        EMPTY;
    }

    private final BaseEnvironment base;
    private final Map<String, String> environmentChanges;

    private CommandEnvironment(BaseEnvironment base, Map<String, String> environmentChanges) {
        this.base = base;
        this.environmentChanges = environmentChanges;
    }

    /**
     * Returns a copy of this environment.
     */
    public CommandEnvironment copy() {
        return new CommandEnvironment(base, new HashMap<>(environmentChanges));
    }

    /**
     * Returns an empty environment with no changes.
     */
    public static CommandEnvironment emptyEnvironment() {
        return new CommandEnvironment(BaseEnvironment.EMPTY, new HashMap<String, String>());
    }

    /**
     * Returns a default environment with no changes.
     */
    public static CommandEnvironment defaultEnvironment() {
        return new CommandEnvironment(BaseEnvironment.DEFAULT, new HashMap<String, String>());
    }

    /**
     * Sets the given variable in this environment.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     *
     * @return this {@code CommandEnvironment}
     */
    public CommandEnvironment set(String name, String value) {
        environmentChanges.put(name, value);
        return this;
    }

    /**
     * Sets all the variables defined by the given map in this environment.
     *
     * @param env a map from variable names to values
     *
     * @return this {@code CommandEnvironment}
     */
    public CommandEnvironment setAll(Map<String, String> env) {
        environmentChanges.putAll(env);
        return this;
    }

    /**
     * Returns this environment's {@linkplain BaseEnvironment base}.
     */
    public BaseEnvironment getBase() {
        return base;
    }

    /**
     * Determines if this environment is the unmodified default environment.
     *
     * @return {@code true} if this environment uses the default base and has no
     *         changes
     */
    public boolean isDefault() {
        return base.equals(BaseEnvironment.DEFAULT) && environmentChanges.isEmpty();
    }

    /**
     * Returns an unmodifiable view of the changes in this environment.
     */
    public Map<String, String> getChanges() {
        return Collections.unmodifiableMap(environmentChanges);
    }
}
