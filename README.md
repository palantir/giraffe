<img src="docs/src/static/logo.png?raw=true" alt="The Giraffe logo, a giraffe wearing glasses" width="120" height ="120"/>

# Giraffe [![Build Status](https://travis-ci.org/palantir/giraffe.svg?branch=develop)](https://travis-ci.org/palantir/giraffe)

Gracefully Integrated Remote Access For Files and Execution
> A long neck to reach far-away places

- [Home](http://palantir.github.io/giraffe/)
- [Documentation](http://palantir.github.io/giraffe/docs/0.8.0/)

## Overview

Giraffe is a Java library that integrates local and remote file system access
with command execution behind a common, familiar API. It combines new classes
for command execution with remote implementations of the `java.nio.file` API
introduced in Java 7.

```java
SshHostAccessor ssh = SshHostAccessor.forPassword("example.com", "giraffe", "l0ngN3ck");

try (HostControlSystem hcs = ssh.open()) {
    Path logs = hcs.getPath("server/logs");
    Files.copy(logs.resolve("access.log"), Paths.get("log/example-access.log"));

    Command archive = hcs.getCommand("server/bin/archive.sh", "--format=zip", "logs");
    Commands.execute(archive);
}
```

## Get Giraffe

Giraffe is available from [JCenter][jcenter] or Palantir's [Bintray repository][bintray].

With **Gradle**:

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'com.palantir.giraffe:giraffe-ssh:0.8.0'
    // or 'com.palantir.giraffe:giraffe-core:0.8.0' for local features only
}
```

[jcenter]: https://bintray.com/bintray/jcenter
[bintray]: http://dl.bintray.com/palantir/releases

## Why Giraffe?

Why did we write Giraffe and why might you use it?

While working on deployment and test tools we found many situations where we
wanted to write code that worked easily on both the local host and remote hosts
using SSH. This required at least three different APIs with different
abstractions:

1. Native Java functionality (with third-party utilities) for local files
2. Native Java functionality or commons-exec for local command execution
3. An SSH library (sshj, jsch, ganymed-ssh2) for remote file manipulation and
   command execution

This led to duplicated abstraction layers in our projects and complicated code
that had to know what type of host it was targeting.

With Giraffe, a single library is required and there are only two APIs: native
Java functionality for files and an intentionally similar API for command
execution.

### Alternatives

The closest equivalent to Giraffe is XebiaLabs's [Overthere][overthere]. In our
view, Giraffe has two major benefits when compared to Overthere:

1. It uses the standard `java.nio.file` API introduced in Java 7
2. It's offered under the Apache 2.0 license instead of the GPLv2

That said, _Overthere_ supports more protocols and supports Windows, which may
make it more appropriate for your use case.

[commons-exec]: https://commons.apache.org/proper/commons-exec/
[sshj]: https://github.com/hierynomus/sshj
[jsch]: http://www.jcraft.com/jsch/
[ganymed-ssh2]: https://code.google.com/p/ganymed-ssh-2/
[overthere]: https://github.com/xebialabs/overthere

## Support

In general, any release of Giraffe is supported until a newer version is
released. Users should update to newer versions as soon as possible.

Occasionally, we continue to provide bug fixes and support for the previous
major version series after a new major version release. These releases are
listed below. We support old releases for at most 6 months after a new major
version release.

*No supported old releases at this time*

## Development

Giraffe builds with Gradle and is configured to use Eclipse as an IDE:

```shell
$ ./gradlew eclipse     # generate Eclipse projects
$ ./gradlew build       # compile libraries and run tests
```

See `./gradlew tasks` for more options.
