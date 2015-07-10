*******
Giraffe
*******

This is the documentation for version |release| of Giraffe. For older or newer
versions, see the `main site`_.

.. _main site: http://palantir.github.io/giraffe/

Installing
==========

Giraffe is published as a Maven-style artifact, but we recommend using
Gradle_ for builds::

    repositories {
        mavenCentral()
        maven {
            url 'http://dl.bintray.com/palantir/releases'
        }
    }

    dependencies {
        compile 'com.palantir.giraffe:giraffe-core:0.6.0'
        compile 'com.palantir.giraffe:giraffe-ssh:0.6.0'
    }

.. _Gradle: https://gradle.org/

Overview
========

New users or users looking for a summary of functionality should read
:doc:`guide`. This will enable you to start writing code that uses Giraffe and
give you the background assumed by the rest of the documentation.

.. toctree::
   :maxdepth: 2

   guide

Learn More
==========

For details on best practices and anti-patterns, see :doc:`effective_giraffe`.
For low-level details, see the full `API documentation`_.

.. toctree::
   :maxdepth: 2

   effective_giraffe
   public_api

.. _API documentation: api/index.html

Release History
===============

The list of changes in this release and all previous releases can be found in
the :doc:`changelog`.

.. toctree::
   :hidden:
   :maxdepth: 2

   changelog
