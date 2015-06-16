package com.palantir.giraffe.file.base.attribute;

/**
 * A type of change to a file's permissions.
 *
 * @author bkeyes
 */
public enum PermissionChange {
    ADD("+"), REMOVE("-"), SET("=");

    private final String operator;

    private PermissionChange(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
