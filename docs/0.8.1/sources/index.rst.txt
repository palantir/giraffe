*******
Giraffe
*******

This is the documentation for version |release| of Giraffe. For older or newer
versions, see the `main site`_.

.. _main site: http://palantir.github.io/giraffe/

Installing
==========

Giraffe is published as a Maven-style artifact, but we recommend using
Gradle_ for builds:

.. code-block:: groovy

    repositories {
        jcenter()
    }

    dependencies {
        compile 'com.palantir.giraffe:giraffe-ssh:0.8.1'
        // or 'com.palantir.giraffe:giraffe-core:0.8.1' for local features only
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
For low-level details, see the full :doc:`api/index`.

.. toctree::
   :maxdepth: 2

   effective_giraffe
   api/index

Release History
===============

The list of changes in this release and all previous releases can be found in
the :doc:`changelog`. To understand how version numbers correspond to API
changes, see :doc:`version_numbers`.

.. toctree::
   :hidden:
   :maxdepth: 2

   changelog
   version_numbers
