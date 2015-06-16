package com.palantir.giraffe.command;

/**
 * Runtime exception thrown when an attempt is made to create an execution
 * system that already exists.
 *
 * @author bkeyes
 */
public final class ExecutionSystemAlreadyExistsException extends RuntimeException {

    public ExecutionSystemAlreadyExistsException() {}

    public ExecutionSystemAlreadyExistsException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -995220310977959332L;
}
