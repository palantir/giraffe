.. image:: logo.svg
   :alt: The Giraffe logo, a Giraffe wearing glasses
   :class: giraffe-logo

*****************
Giraffe |version|
*****************

Gracefully Integrated Remote Access For Files and Execution

.. epigraph::
   A long neck to reach far-away places

Overview
========

Giraffe is a Java library that integrates local and remote file system access
with command execution behind a common, familiar API. It combines new classes
for command execution with remote implementations of the ``java.nio.file`` API
introduced in Java 7.

Giraffe makes it easy to write code that works with both local and remote
hosts::

    void configureAndStartServer(Path serverRoot, ExecutionSystem es) {
        Files.copy(Paths.get("server.yaml"), serverRoot.resolve("server.yaml"));
        Files.copy(Paths.get("db.yaml"), serverRoot.resolve("db.yaml"));

        Commands.execute(es.getCommand(serverRoot.resolve("bin/start.sh"), 8080)):
    }

    // local
    configureAndStartServer(Paths.get("/opt/share/gazelle"), ExecutionSystems.getDefault());

    // remote
    Host host = Host.fromHostname("gazelle.example.com");
    SshHostAccessor ssh = SshHostAccessor.authWithKey(host, "admin", Paths.get("/home/admin/.ssh/id_rsa"));
    try (HostControlSystem hcs = ssh.open()) {
        configureAndStartServer(hcs.getPath("/opt/gazelle"), hcs.getExecutionSystem());
    }

Get Giraffe
===========

For **Gradle** projects, add the following dependencies::

    dependencies {
       compile 'com.palantir.giraffe:giraffe-core:0.6.0'
       compile 'com.palantir.giraffe:giraffe-ssh:0.6.0'
    }

Learn More
==========

* Javadoc_ - API documentation
* :doc:`guide` - an introduction to the major features and how to use them
* :doc:`effective_giraffe` - a discussion of best practices and antipatterns
* :doc:`public_api` - describes how the API relates to the version number
* :doc:`changelog` - release history and changelog

.. _Javadoc: api/index.html

.. toctree::
   :hidden:
   :maxdepth: 2

   guide
   effective_giraffe
   public_api
   changelog
