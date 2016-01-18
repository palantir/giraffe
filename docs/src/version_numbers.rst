*********************
About Version Numbers
*********************

Giraffe is `semantically versioned <semver_>`_ with respect to the *client* public
API. It follows a modified scheme for the *extender* public API.

The public APIs and version schemes are defined for clarity and completeness.
As a result, many "major" releases may be safe, routine updates most users.
Always check the changelog before deciding whether or not to update to a new
release.

Client API
==========

The client API defines compatibility for users who do *not* extend any
Giraffe classes or interfaces and includes:

1. All public classes and interfaces in the following packages, the methods of
   these types, and any behavior documented by or in relation to these types.

   * ``com.palantir.giraffe``
   * ``com.palantir.giraffe.command``
   * ``com.palantir.giraffe.command.interactive``
   * ``com.palantir.giraffe.command.spi``
   * ``com.palantir.giraffe.file``
   * ``com.palantir.giraffe.host``
   * ``com.palantir.giraffe.ssh``

2. The existence of dependencies exposed by the types in (1)
3. The minimum versions of the dependencies in (2)

Incompatible [#comp]_ changes to any of these items result in a new MAJOR version
number. Incompatible changes to the client API are primarily removals. In other
words, any change that breaks code that calls methods on Giraffe types.

Extender API
============

The extender API defines compatibility for users who extend Giraffe
classes, either to add functionality or to implement a system provider. It
includes:

1. All public classes and interfaces in the packages defined by the client API
   and the following packages, the methods of these types, and any behavior
   documented by or in relation to these types.

   * ``com.palantir.giraffe.file.base``

2. The existence of dependencies exposed by the types in (1)
3. The minimum versions of the dependencies in (2)

Incompatible changes to any of these items that are *not* part of the client
API result in a new MINOR version number. Incompatible changes to the
extender API are primarily additions to client API classes and removals,
additions, and modifications to other classes. In other words, any change that
breaks code that extends or implements Giraffe types.

Modifications
=============

We reserve the ability to change any undocumented implementation details in the
interest of fixing bugs, improving code quality, or improving performance. If
you depend on undocumented observed behavior that you think should be defined,
please file an issue.

We also reserve the ability to add, remove, or update dependencies that are not
exposed by public types.

The definition of the public API may expand at any time. Removing any elements
from the definition requires a major version number change.

.. _semver: http://semver.org/

.. rubric:: Footnotes

.. [#comp]
   For classes and interfaces in both APIs, "incompatible" refers to *compile*
   compatibility, not *binary* compatibility.
