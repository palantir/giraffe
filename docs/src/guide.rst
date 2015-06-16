**************
Giraffe Basics
**************

This guide covers the basic features of Giraffe and is a good introduction for
new users. For advanced topics and more details, see :doc:`effective_giraffe`.

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

TODO

Executing Interactive Commands
==============================

TODO

Remote Systems
==============

TODO
