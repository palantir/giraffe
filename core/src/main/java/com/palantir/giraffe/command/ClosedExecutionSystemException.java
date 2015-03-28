package com.palantir.giraffe.command;

/**
 * Runtime exception thrown when methods are called on a closed execution
 * system.
 *
 * @author bkeyes
 */
public final class ClosedExecutionSystemException extends IllegalStateException {

    public ClosedExecutionSystemException() {}

    private static final long serialVersionUID = 7257195953396698323L;
}
