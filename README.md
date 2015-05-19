<img src="docs/src/logo.png?raw=true" alt="The Giraffe logo, a giraffe wearing glasses" width="160" height ="160"/>

# Giraffe

Gracefully Integrated Remote Access For Files and Execution
> A long neck to reach far-away places

## Overview

Giraffe is a Java library that integrates local and remote file system access
with command execution behind a common, familiar API. It combines new classes
for command execution with remote implementations of the `java.nio.file` API
introduced in JDK7.

    Host example = Host.fromHostname("example.com");
    SshHost<?> ssh = SshHost.authWithPassword(example, "giraffe", "l0ngN3ck");

    try (HostControlSystem hcs = HostControlSystems.openRemote(ssh)) {
        Path logs = hcs.getPath("server/logs");
        Files.copy(logs.resolve("access.log"), Paths.get("log/example-access.log");

        Command archive = hcs.getCommand("server/bin/archive.sh", "--format=zip", "logs");
        Commands.execute(archive);
    }

## Using Giraffe

Most projects that use Giraffe include the `core` project and one or more
remote implementations. Currently, `ssh` is the only remote implementation.

In **Gradle**:

    dependencies {
        compile 'com.palantir.giraffe:giraffe-core:0.6.0'

        // remote implementations
        compile 'com.palantir.giraffe:giraffe-ssh:0.6.0'
    }

## Versions and Public API

Giraffe is [semantically versioned][semver] with respect to the _client_ public
API. It follows a modified scheme for the _extender_ public API.

The public APIs and version schemes are defined for clarity and completeness.
As a result, many "major" releases may be safe, routine updates most users.
Always check the changelog before deciding whether or not to update to a new
release.

The **Client API** defines compatibility for users who do _not_ extend any
Giraffe classes or interfaces and includes:

1. All public classes and interfaces in the following packages, the methods of
   these types, and any behavior documented by or in relation to these types.

    * `com.palantir.giraffe`
    * `com.palantir.giraffe.command`
    * `com.palantir.giraffe.command.interactive`
    * `com.palantir.giraffe.command.spi`
    * `com.palantir.giraffe.controller`
    * `com.palantir.giraffe.file`
    * `com.palantir.giraffe.host`
    * `com.palantir.giraffe.ssh`

2. The existence of dependencies exposed by the types in (1)
3. The minimum versions of the dependencies in (2)

Incompatible changes to any of these items result in a new MAJOR version
number. Incompatible changes to the client API are primarily removals. In other
words, any change that breaks code that calls methods on Giraffe types.

The **Extender API** defines compatibility for users who extend Giraffe
classes, either to add functionality or to implement a system provider. It
includes:

1. All public classes and interfaces in the packages defined by the client API
   and the following packages, the methods of these types, and any behavior
   documented by or in relation to these types.

    * `com.palantir.giraffe.file.base`

2. The existence of dependencies exposed by the types in (1)
3. The minimum versions of the dependencies in (2)

Incompatible changes to any of these items that are _not_ part of the client
API result in a new MINOR version number. Incompatible changes to the
extender API are primarily additions to client API classes and removals,
additions, and modifications to other classes. In other words, any change that
breaks code that extends or implements Giraffe types.

For classes and interfaces in both APIs, "incompatible" refers to _compile_
compatibility, not _binary_ compatibility.

While Giraffe is in initial development (0.y.z), we make a best-effort attempt
to update the minor version number as if it were the major version when
changing the public API.

We reserve the ability to change any undocumented implementation details in the
interest of fixing bugs, improving code quality, or improving performance. If
you depend on undocumented observed behavior that you think should be defined,
please file an issue.

We also reserve the ability to add, remove, or update dependencies that are not
exposed by public types.

The definition of the public API may expand at any time. Removing any elements
from the definition requires a major version number change.

[semver]: http://semver.org/

## Support

In general, any release of Giraffe is supported until a newer version is
released. Users should update to newer versions as soon as possible.

Occasionally, we continue to provide bug fixes and support for the previous
major version series after a new major version release. These releases are
listed below. We support old releases for at most 6 months after a new major
version release.

*No supported old releases at this time*

