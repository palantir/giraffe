**************
Giraffe Basics
**************

This guide covers the basic features of Giraffe and is a good introduction for
new users.

.. javaimport::
   java.nio.file.*
   com.palantir.giraffe.command.*
   com.palantir.giraffe.file.*
   com.palantir.giraffe.host.*

Accessing Files
===============

To do anything with a file or directory, you need a :javaref:`Path`. A ``Path``
combines a path string with a :javaref:`FileSystem` that can interpret the
string. This configuration isn't unique to Giraffe and is actually part of the
:javaref:`java.nio.file` API introduced in Java 7.

You can get a path from a ``FileSystem`` or, if you are only using the local
file system, a static factory method::

    // local paths
    Path logo = Paths.get("resources/images/kittens.png");
    Path betterLogo = Paths.get("resources", "images", "kittens.gif");

    // local or remote, depending on fileSystem
    Path bestLogo = fileSystem.getPath("resources/images", "laser_kittens.gif");

The factory methods take full path strings, path components, or some mix of the
two. Path strings must use the same syntax as the file system, so providing
individual components is safer with systems of unknown origin.

You can also build a path by adding to an existing one::

    Path images = Paths.get("resources/images");
    Path sloths = images.resolve("sloths.gif");

Because ``Path`` is immutable, methods like ``resolve`` return new objects and
the original path is not modified.

To manipulate or access the file or directory given by a path, use the methods
in :javaref:`Files` and :javaref:`MoreFiles`::

    Path data = Paths.get("/var/data");
    Files.createDirectory(data);
    MoreFiles.writeString(data.resolve("json.yaml"), getJson(), StandardCharsets.UTF_8);
    Files.copy(Paths.get("text.bin"), data.resolve("bin.sh"));

Take some time to explore the full APIs for these two classes, as they define
all of the operations you can perform on files and directories.

.. note::
   The methods on ``Files`` and ``MoreFiles`` only work with paths from open file
   systems. Once you close a file system, all ``Path`` objects associated with
   it are effectively useless.

Executing Commands
==================

To execute any external process, you need a :javaref:`Command`. A ``Command``
combines the name or path of an executable with a set of arguments and a
:javaref:`ExecutionSystem` that can run the executable.

.. tip::
   The command execution API intentionally follows the pattern of the file
   system API, with ``Command`` taking the place of ``Path`` and
   ``ExecutionSystem`` taking the place of ``FileSystem``. If you understand
   how to use one, the other should feel familiar.

You can get a command from an ``ExecutionSystem`` or, if you are only running
executables on the local system, a static factory method::

    // local commands
    Command server = Commands.get("bin/kitten.sh", "--verbose", "string.txt");
    Command betterServer = Commands.get(Paths.get("bin/freeKitten.sh"), "--debug", "--type", "spiders");

    // local or remote, depending on execSystem
    Command bestServer = execSystem.getCommand("laserKitten.sh", "-n", 1000, "-o", "/tmp/litter");

The executable can be a name, a path string, or a ``Path``. When given a name,
a system-dependent method is used to find the executable, usually using the
value of the ``PATH`` environment variable. Any additional parameters are
passed to the command as arguments. Arguments can have any type and are
automatically converted to strings and :ref:`escaped <command-escaping>`.

For commands with more complicated arguments, use :javaref:`Command.Builder`::

    Command.Builder builder = Commands.getBuilder("sloth-parse");
    builder.addArgument("--use-tree");
    builder.addArguments("--speed", "slow", "-f", Paths.get("leaves.sloth"));

    List<String> outputArgs = getOutputArgs();
    builder.addArguments(outputArgs);

    Command slothParse = builder.build();

To run a ``Command``, use the methods in :javaref:`Commands`::

    Command zeros = Commands.get("ones.py", "--negate", 8);
    CommandResult zeroResult = Commands.execute(zeros);
    assertEquals("00000000", zeroResult.getStdOut());

    Command ones = Commands.get("ones.py", "--high-precision", 10000);
    CommandFuture future = Commands.executeAsync(ones);
    doImportantThings();
    CommandResult oneResult = Commands.waitFor(future);

``Commands`` also provides methods to execute commands with timeouts or with a
modified environment. 

By default, the various ``execute`` methods assume that successful commands
exit with status ``0``, throwing :javaref:`CommandException` when commands
exit with a different status. To change this behavior, use a method that takes
a ``CommandContext``, setting it to ignore the exit status or check for a
different condition.

