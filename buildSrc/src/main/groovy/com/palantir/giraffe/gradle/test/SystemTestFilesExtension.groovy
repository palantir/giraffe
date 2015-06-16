package com.palantir.giraffe.gradle.test;

import org.gradle.api.NamedDomainObjectContainer;

class SystemTestFilesExtension {
    final NamedDomainObjectContainer<TestFileCreator> creators

    File filesDir

    SystemTestFilesExtension(creators) {
        this.creators = creators
    }

    void creators(Closure closure) {
        creators.configure(closure)
    }
}
