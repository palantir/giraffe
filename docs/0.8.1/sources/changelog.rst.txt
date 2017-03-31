*********
Changelog
*********

0.8.1
=====

Fixes and Improvements
----------------------

- Fix buffer resize bug that could lead to an ``IOException``
- Upgrade Gradle to the latest version and modernize build tooling

0.8.0
=====

`GitHub Milestone <https://github.com/palantir/giraffe/milestones/0.8.0>`__

New Features
------------

- Add support for Kerberos authentication
- Add ``MoreFiles.listDirectoryRecursive``
- Add ``MoreFiles.listDirectoryFiles`` which excludes child directories

Fixes and Improvements
----------------------

- Update SSHJ to 0.15.0
- Check for loops in recursive copy
- Fix recursive copy on Windows
- Fix typo in ``AbstractCommand#toString()``

0.7.0
=====

Initial open-source release! `GitHub Milestone <https://github.com/palantir/giraffe/milestones/0.7.0>`__

New Features
------------

- Add ``HostAccessor``, a factory for ``HostControlSystem`` instances
- Add ``Commands.isLocal`` and ``MoreFiles.isLocal``

API Changes
-----------

- Change URIs of SSH systems to ``file+ssh`` and ``exec+ssh``
- Replace ``SshHost`` with ``SshHostAccessor``
- Rename SSH credential classes to ``PasswordSshCredential`` and
  ``PublicKeySshCredential``
- Remove ``HostControlSystems``; use ``HostAccessors.getDefault().open()`` to
  get the default ``HostControlSystem``
- Replace ``SystemConverter`` with ``SystemUpgrader``
- Remove reverse DNS resolution during ``Host`` creation

Fixes and Improvements
----------------------

- Fix integer overflow when resizing buffers
- Reads from ``CommandFuture`` streams no longer consume output that would
  otherwise be available when creating ``CommandResult`` instances.
- Add windowing to standard output and error streams, allowing clients to
  optionally discard data
- Fix size attribute for empty files when using SSH
- Throw ``ClosedExecutionSystemException`` when calling methods on a closed
  execution system
- Improve exception message when a command times out
- Many build and documentation changes

0.6.0
=====

New Features
------------

- Add ``Commands.toResult``

API Changes
-----------

- Redesign interactive execution API: ``ShellConversation`` and
  ``CommandOutputTrigger`` now use ``ResponseProvider`` implementations to get
  responses for specific output
- Update Guava to 18.0; earlier versions work but are not supported

Fixes and Improvements
----------------------

- Fix serious buffering bug that could cause incorrect command input and output
- Fix issue when recursively removing permissions from local directories
- GIRAFFE-18: Improve localhost resolution when creating ``Host`` objects
- Update to SLF4J 1.7.9 and fix version conflict
- Improve and publish documentation

0.5.0
=====

New Features
------------

- Add ``SystemConverter`` utility. This allows users to convert a file system
  into an execution system and vice versa with minimal resource duplication.
- Fix GIRAFFE-6: Add ``MoreFiles.deleteRecursiveIfExists``

API Changes
-----------

- ``RemoteHostAccessor`` now opens ``HostControlSystem`` instances instead of
  individual file and execution systems
- Rename ``CommandEnvironment`` methods
- Change ``CommandBuilder#addArguments`` signature to avoid overload conflict
- Remove 'giraffe-controllers' project; ``PreferencesFile`` is now part of a
  separate library

Fixes and Improvements
----------------------

- Fix GIRAFFE-13: Set write mode for files when copying between SSH hosts
- Fix issue that prevented some exceptions from cancelling execution
- Improve SSH process cancellation
- Register shutdown hooks for local processes
- Improve documentation for many classes

0.4.2
=====

- GIRAFFE-12: Add support for local environment filtering. To enable, set the
  ``giraffe.command.local.envWhitelist`` system property to a comma-separated
  list of variables allowed in command process environments.
- Include host information in SSH debug logs. Use the ``giraffe-ssh-host`` MDC
  key in appender patterns to see this information.

0.4.1
=====

- Fix GIRAFFE-11: ``TempPath#close()`` failed if the path did not exist
- Fix stack traces of exceptions thrown by execute methods in ``Commands``.
  Previously, these exceptions only had internal stack traces, meaning
  information about the caller was lost.

0.4.0
=====

Command Execution
-----------------

- Include more information in ``CommandException`` messages, including the full
  command output and the URI of the execution system
- Rename ``Command#getCommand()`` to ``getExecutable()``
- Change ``Command#getArguments()`` to return a list of strings
- Implement ``toString()``, ``equals()``, and ``hashCode()`` for ``Command``
- Move command working directory to ``CommandContext``
- Support working directories for SSH commands
- Remove dependency on Apache commons-exec
- Fix GIRAFFE-9: properly escape local command arguments

Other Changes
-------------

- Add SSH system debug logging
- Add ``MoreFiles#copyLarge`` which significantly improves the speed of large
  file transfers between the local machine and SSH hosts
- Significantly improve the speed of certain recursive copy operations
- Convert ``HostControlSystem`` to an interface. Move creation methods to
  ``HostControlSystems``.

0.3.6
=====

- Fix missing provider issue when opening an SSH execution system from an SSH
  file system with a non-standard class loader configuration

0.3.5
=====

- Fix GIRAFFE-10: closing an SSH system does not stop all threads

0.3.4
=====

- Add additional factory methods to ``CommandContext``
- Fix ``deleteIfExists`` for SSH implementation
- Fix GIRAFFE-5: localhost lookup can fail on OS X
- Standardize exit status checks for command execution. Exit status is now
  checked by all ``execute`` methods and throws consistent exceptions.

0.3.3
=====

- Fix ``UniformPath`` to ``Path`` conversion

0.3.2
=====

- Fix escaping for arguments that start with a single quote
- Fix argument check that broke reading output from local commands

0.3.1
=====

- Fix bug when using append mode to write to an empty file via SSH

0.3.0
=====

Command Execution
-----------------

- Add asynchronous command execution
- Implement timeout handling for commands. Timeouts are specified when calling
  ``execute`` methods, rather than as part of the context.
- Remove ``executeUnverified``. Instead, disable exit status checking in
  ``CommandContext``.
- Add interactive command execution. Use ``ShellConversation`` to interact with
  commands or use ``CommandOutputTrigger`` to run arbitrary code when command
  output matches a pattern.

Other Breaking Changes
----------------------

- Rename ``giraffe.remote`` package to ``giraffe.host``
- Rename ``Files2`` to ``MoreFiles``

Other Changes
-------------

- Use SSHJ to implement SSH operations. This library provides a cleaner API,
  better functionality, and more active development.
- Improve performance of recursive permission operations
- Add ``listDirectory`` and ``isEmpty`` to MoreFiles
- Fix unexpected output from ``MoreFiles.write``
- Expose information stored in SSH credential objects to clients
- Improve file system test coverage

0.2.1
=====

- Fix issues reading SSH streams with certain buffer sizes
- Fix local execution system URI
- Add ability to execute commands with arbitrary environments
- Use ``UniformPath`` for command working directories
- Improve API documentation

0.2.0
=====

- Remove caching for SSH systems, allowing clients to open multiple systems for
  a given host
- Remove interactive command execution since it is not supported. This will be
  added again in a later release
- Remove ``Accessible`` interfaces; replace ``SystemAccessible`` with
  ``RemoteHostAccessor``
- Add ``HostControlSystem``, a container for a ``FileSystem`` and
  ``ExecutionSystem`` targeting the same host
- Add ``getCommand(Path, Object...)`` overload
- Fix classloading issue when ``SshHost`` is loaded in a non-standard
  classloader

0.1.3
=====

- Fix race condition when reading SSH command output
- ``Files2.defaultDirectory`` returns an absolute path
- Exclude the ``.`` and ``..`` meta-entries from directory streams

0.1.2
=====

- Fix reading output from SSH commands
- Add missing separator between ``first`` and ``more`` path components

0.1.1
=====

- Downgrade Ganymed SSH2 to build251beta1 for better compatibility with other
  projects
- Fix extra leading slash in absolute SSH paths
- Fix ``getParent()`` for single-element paths
- Throw exception if SSH authentication fails

0.1.0
=====

Core Features
-------------

- Support basic local command execution
- Support basic remote command execution over SSH
- Support common remote file system operations over SSH

Utilities
---------

- Add ``Files2`` to supplement ``java.nio.file.Files``
- Add ``TempPath`` for auto-deleting temporary files
- Add ``PreferencesFile``
- Add ``UniformPath``, a ``java.nio.file.Path``-like object without a file
  system reference

Base File System
----------------

Utilities for custom file system implementations

- Annotation based attribute access
- Glob-to-regex conversion
- Immutable, list-based ``java.nio.file.Path`` implementation
