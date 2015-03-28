*****************
Effective Giraffe
*****************

While we hope the Giraffe API is easy to understand and use, there are certain
best practices and pitfalls that are worth discussing explicitly. This guide
outlines patterns to avoid, highlights potentially surprising behavior, and
gives examples of "giraffesque" code. It also discusses the rational for API
choices when appropriate or enlightening.

.. javaimport::
   java.nio.file.*
   com.palantir.giraffe.command.*
   com.palantir.giraffe.file.*
   com.palantir.giraffe.host.*

Connection Management
=====================

Always close opened connections
-------------------------------

Giraffe leaves connection management to clients: connections are never
automatically opened and are only automatically closed under certain error
conditions. This is the simplest way to guarantee compatibility with a wide
range of applications, but means that clients are responsible for closing all
connections that they open. Use the *try-with-resources* statement to simplify
this::

    SshHost<?> host = getHost();
    try (HostControlSystem hcs = HostControlSystems.openRemote(host)) {
        Files.copy(hcs.getPath("transformers.mp4"), hcs.getPath("transformers2.mp4"));
        // ...
    }

If *try-with-resources* is impractical, call ``close`` explicitly.

.. note::
   If a program does not terminate when expected, be sure that all systems are
   closed correctly. Many systems start non-daemon threads that are only
   stopped when the system closes.

Avoid leaking system references
-------------------------------

Both :javaref:`Path` and :javaref:`Command` are "live" objects, meaning they
are associated with active file systems and active execution systems
respectively. A ``Path`` associated with a closed file system is only useful
for inspection — passing it to any method in :javaref:`Files` will throw an
exception.

Avoid leaking references to these objects outside the scope of the
*try-with-resources* statement that opens and closes the associated system.

Consider :ref:`using UniformPath <uniform-path>` to avoid this problem when
passing path information.

Maximize connection lifetime
----------------------------

Opening remote connections is expensive, so they should be created as
infrequently as possible and left open for as long as is practical. Open
connections at the highest reasonable level in an application and pass open
systems (or objects that reference open systems) into subroutines rather than
creating new connections in each subroutine.

Use ``HostControlSystem`` when mixing local and remote hosts
------------------------------------------------------------

The first two guidelines do not apply to the local file system or local
execution system. These systems are always open and their ``close`` methods
always throw exceptions. Because remote systems must always be closed, code
that deals with both local and remote hosts must handle this distinction to
avoid generating errors at runtime. Use :javaref:`HostControlSystem` instead of
individual file and execution systems to abstract this difference::

    // custom host representation for both local and remote hosts
    interface MyHost {
        boolean isLocal();
        SshHost<?> getSshHost();
    }

    public HostControlSystem openSystem(MyHost host) throws IOException {
        if (host.isLocal()) {
            return HostControlSystems.getDefault();
        } else {
            return HostControlSystems.openRemote(host.getSshHost());
        }
    }

    MyHost host = getHost();
    try (HostControlSystem hcs = openSystem(host)) {
        Path path = hcs.getPath("potato.txt")
        // ...
    }

The local (default) ``HostControlSystem`` implements a no-op ``close`` method
that is safe to call.

Exception Handling
==================

Most methods on Giraffe classes throw ``IOException`` or more specific
subclasses. By their nature, file system operations and command execution rely
on external resources and can fail for reasons outside the control of the
executing code. While dealing with these exceptions may seem annoying, hiding
them would create a broken API that does not reflect reality.

Unless there is an appropriate response to a local failure, propagate
exceptions up to the level at which the system was created, where they can be
handled in a uniform way as part of the existing *try-with-resources*
statement::

    public void copyThings(Path target) throws IOException {
        // ...
    }

    public void copyStuff(Path target) throws IOException {
        // ...
    }

    public void startServer() throws IOException {
        // ...
    }

    try (HostControlSystem hcs = HostControlSystems.openRemote(host)) {
        copyThings(hcs.getPath("things"));
        copyStuff(hcs.getPath("stuff"));
        startServer();
    } catch (IOException e) {
        log.error("Failed to setup and start server", e);
        throw new IllegalStateException(e);
    }

.. tip::
   When using Giraffe for testing, allow test methods to throw ``IOException``.
   This removes unnecessary *try-catch* statements and generally has the same
   result: the test fails if any operation throws an exception.

File Operations
===============

Only use ``Paths.get`` for local paths
--------------------------------------

:javaref:`Paths.get <Paths#get(String, String...)>` is a convenient way to
create *local* paths. These paths are *not* compatible with other file systems.
In particular, avoid these patterns::

    Path local = Paths.get("docs/cheetah/running.txt");
    try (HostControlSystem hcs = HostControlSystems.openRemote(remoteHost)) {
        Path remote = hcs.getPath("animals");

        // BAD - throws exception
        Path target = remote.resolve(local);

        // BAD - only works if path syntax (separator, etc.) is the same
        Path otherTarget = remote.resolve(local.toString());
    }

Safely resolving a local path with a remote path requires more effort::

    Path local = Paths.get("docs/cheetah/running.txt");
    try (HostControlSystem hcs = HostControlSystems.openRemote(remoteHost)) {
        Path remote = hcs.getPath("animals");

        Path target = remote;
        for (Path segment : local) {
            target = target.resolve(segment.getFileName().toString());
        }
    }

.. _uniform-path:

Use ``UniformPath`` when no file system is available
----------------------------------------------------

:javaref:`UniformPath` is a ``Path``-like object that is *not* associated with
any file system. It defines a consistent syntax for path strings and can be
converted to and from real paths. Consider using ``UniformPath`` when opening a
real file system is impractical or when paths from multiple file systems are
combined.

Know and use library methods
----------------------------

The majority of Giraffe's file system operations are provided by
:javaref:`Files`, which is part of Java's standard library. Familiarity with
these methods is key to writing effective file manipulation code. Prefer
library methods over custom versions or command execution.

Giraffe supplements the standard methods with methods defined in
:javaref:`MoreFiles`. These methods implement recursive operations and other
useful functionality that is omitted by the standard library. In addition to
being more convenient, methods in ``MoreFiles`` are often significantly faster
than custom implementations of the same functionality.

Command Execution
=================

Don't escape arguments
----------------------

Giraffe automatically escapes all command arguments as required by the target
platform. This means that the literal arguments provided in Java code are
passed to commands. For example, ``printargs.sh`` prints each argument on a
separate line::

    Commands.execute(Commands.get("printargs.sh", "a", "b c", "d")).getStdOut();
    // => a
    //    b c
    //    d

    Commands.execute(Commands.get("printargs.sh", "'a'", "\"b c\"")).getStdOut();
    // => 'a'
    //    "b c"

    Commands.execute(Commands.get("printargs.sh", "b && $c", "|", "> out")).getStdOut();
    // => b && $c
    //    |
    //    > out

Only manually escape arguments if a specific command requires special escape
sequences.

Execute shell commands in a shell
---------------------------------

Argument escaping has an important consequence for pipelines and commands that
use shell behaviors like variable expansion. Because all arguments are passed
as literals, the shell will not interpret any special characters. Explicitly
execute a command in a shell when shell behavior is required::

    Commands.get("sh", "-c", "cat file.txt | grep ${WORD} > out.txt");

Use shell commands judiciously. Prefer processing data and arguments in Java,
as this is often easier to understand for readers and reduces dependencies on
external utilities, which may have different behavior on different platforms.

Many uses of shell commands can be replaced by :javaref:`CommandContext`. Use
this class to change the working directory of commands and the values of
environment variables instead of changing these as part of a shell expression.

Never catch ``CommandException``
--------------------------------

By default, the ``execute`` methods in :javaref:`Commands` throw an unchecked
:javaref:`CommandException` if the exit status of a command is non-zero.
Instead of catching this exception, disable exit status checks using
:javaref:`CommandContext`::

    Commands.execute(command, CommandContext.ignoreExitStatus())

In this mode, ``execute`` will never throw ``CommandException``. If the command
uses a different value to indicate success, use
:javaref:`CommandContext#requireExitStatus(int)` or provide a custom predicate.

Prefer absolute paths to modifying ``PATH``
-------------------------------------------

If a command is not available on the system path, use an absolute path to refer
to the command instead of modifying the ``PATH`` environment variable using
``CommandContext``. While the modified environment always applies to the
executed command, the use of the environment to find the command to execute is
system-dependent.

For instance, the local execution system uses the value of ``PATH`` that was
set when the JVM started to locate executables and the modified ``PATH`` is
only seen by the new command process. On the other hand, the SSH execution
system uses the modified ``PATH`` value to locate executables because it is set
before the implicit shell tries to find the command.
