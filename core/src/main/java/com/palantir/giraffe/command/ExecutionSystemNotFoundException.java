package com.palantir.giraffe.command;

/**
 * Runtime exception thrown when an execution system cannot be found.
 *
 * @author bkeyes
 */
public final class ExecutionSystemNotFoundException extends RuntimeException {

    public ExecutionSystemNotFoundException() {}

    public ExecutionSystemNotFoundException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -378596008898522613L;
}
