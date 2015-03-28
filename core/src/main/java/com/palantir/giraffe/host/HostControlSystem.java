package com.palantir.giraffe.host;

import java.io.Closeable;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.palantir.giraffe.command.Command;
import com.palantir.giraffe.command.ExecutionSystem;

/**
 * Combines a file system and an execution system for a local or remote host.
 * <p>
 * {@code HostControlSystem}s are open on creation and should be closed after
 * use. Clients should not close the underlying systems individually.
 *
 * @author jchien
 */
public interface HostControlSystem extends Closeable {

    /**
     * Gets a path on this host's file system.
     *
     * @see FileSystem#getPath
     */
    Path getPath(String first, String... more);

    /**
     * Gets a command on this host's execution system.
     *
     * @see ExecutionSystem#getCommand(String, Object...)
     */
    Command getCommand(String executable, Object... args);

    /**
     * Gets a command on this host's execution system.
     *
     * @see ExecutionSystem#getCommand(Path, Object...)
     */
    Command getCommand(Path executable, Object... args);

    /**
     * Returns this host's {@code FileSystem}. The system is open and should not
     * be closed.
     */
    FileSystem getFileSystem();

    /**
     * Returns this host's {@code ExecutionSystem}. The system is open and
     * should not be closed.
     */
    ExecutionSystem getExecutionSystem();

    /**
     * Returns the {@link Host} controlled by this system.
     */
    Host getHost();

    /**
     * Returns the name of the host controlled by this system.
     */
    String getHostname();

}
