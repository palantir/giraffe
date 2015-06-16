package com.palantir.giraffe.command;

import java.net.URI;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Runtime exception thrown when a command fails, usually by exiting with a
 * non-zero exit status.
 * <p>
 * The {@linkplain #getMessage() message} contains detailed information about
 * the command that ran, the execution system that ran it, and the output of the
 * command. This information is also available programatically via getter
 * methods.
 *
 * @author bkeyes
 */
public class CommandException extends RuntimeException {

    private final String executable;
    private final ImmutableList<String> args;
    private final URI uri;
    private final CommandResult result;

    /**
     * Creates an exception with the specified command and result.
     *
     * @param command the command that failed
     * @param result the result describing the failure
     */
    public CommandException(Command command, CommandContext context, CommandResult result) {
        super(makeMessage(command, context, result));

        this.executable = command.getExecutable();
        this.args = command.getArguments();
        this.uri = command.getExecutionSystem().uri();
        this.result = result;
    }

    /**
     * Returns the failed command.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns the arguments of the failed command.
     */
    public ImmutableList<String> getArguments() {
        return args;
    }

    /**
     * Returns the URI of the system that executed the command.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the result of executing the command.
     */
    public CommandResult getResult() {
        return result;
    }

    /**
     * Returns a description of the executed command and how it failed.
     * <p>
     * The description spans multiple lines and includes the:
     * <ul>
     * <li>Exit status</li>
     * <li>Executable name or path</li>
     * <li>Argument list, with each argument surrounded by double quotes</li>
     * <li>Working directory, if not the default</li>
     * <li>Environment, if not the default</li>
     * <li>Execution system URI</li>
     * <li>Contents of the output and error streams</li>
     * </ul>
     *
     * @return a description of the failed command
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    private static String makeMessage(Command command,
                                      CommandContext context,
                                      CommandResult result) {

        StringBuilder msg = new StringBuilder();

        msg.append("exited with unexpected status ").append(result.getExitStatus());
        msg.append(System.lineSeparator());

        indent(msg, 1).append("executable: ").append(command.getExecutable());
        msg.append(System.lineSeparator());

        Iterable<String> escapedArgs = Iterables.transform(command.getArguments(), ARG_ESCAPER);
        indent(msg, 1).append("arguments: ");
        Joiner.on(", ").appendTo(msg.append('['), escapedArgs).append(']');
        msg.append(System.lineSeparator());

        if (context.getWorkingDirectory().isPresent()) {
            indent(msg, 1).append("working dir: ").append(context.getWorkingDirectory().get());
            msg.append(System.lineSeparator());
        }

        CommandEnvironment env = context.getEnvironment();
        if (!env.isDefault()) {
            indent(msg, 1).append("environment: ").append(env.getBase()).append(' ')
                          .append("with changes ").append(env.getChanges());
            msg.append(System.lineSeparator());
        }

        indent(msg, 1).append("execution system: ").append(command.getExecutionSystem().uri());
        msg.append(System.lineSeparator());

        appendOutput(msg, "stderr", result.getStdErr());
        msg.append(System.lineSeparator());
        appendOutput(msg, "stdout", result.getStdOut());
        return msg.toString();
    }

    private static final Function<String, String> ARG_ESCAPER = new Function<String, String>() {
        @Override
        public String apply(String arg) {
            String escaped = arg;
            escaped = escaped.replace("\n", "\\n");
            escaped = escaped.replace("\r", "\\r");
            escaped = escaped.replace("\t", "\\t");
            return '"' + escaped + '"';
        }
    };

    private static void appendOutput(StringBuilder sb, String name, String output) {
        indent(sb, 1).append(name).append(": ");
        if (output.isEmpty()) {
            sb.append("<no output>");
        } else {
            sb.append(output);
        }
    }

    private static final int SPACES_PER_LEVEL = 4;

    private static StringBuilder indent(StringBuilder sb, int level) {
        return sb.append(String.format("%" + level * SPACES_PER_LEVEL + "s", ""));
    }

    private static final long serialVersionUID = 2L;
}
