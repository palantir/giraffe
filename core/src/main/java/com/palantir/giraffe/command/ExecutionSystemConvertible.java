package com.palantir.giraffe.command;

import java.io.IOException;

/**
 * Systems that implement this interface can be converted into
 * {@link ExecutionSystem} instances.
 *
 * @author bkeyes
 */
public interface ExecutionSystemConvertible {

    /**
     * Returns an open {@link ExecutionSystem} that accesses the same resources
     * as this system.
     * <p>
     * The returned system is independent from this system and either can be
     * closed without affecting the other.
     *
     * @throws IOException if an I/O error occurs while creating the new system
     */
    ExecutionSystem asExecutionSystem() throws IOException;

}
