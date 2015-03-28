package com.palantir.giraffe.gradle.test;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

class TestFileCreator {
    final String name
    final Project project

    FileCollection classpath
    String main

    private Project source

    TestFileCreator(String name, Project project) {
        this.name = name
        this.project = project
    }

    String getMain() {
        if (main == null) {
            main = source.systemTest.creatorClass
        }
        return main
    }

    FileCollection getClasspath() {
        if (classpath == null) {
            if (source == project) {
                classpath = project.configurations.systemTestCreator.artifacts.files
            } else {
                def conf = project.configurations.create(name + 'Creator')
                project.dependencies {
                    add(conf.name, project(path: source.path, configuration: 'systemTestCreator'))
                }
                classpath = conf
            }
        }
        return classpath
    }

    void source(Project source) {
        this.source = source
    }

    String getTaskName() {
        return name[0].toUpperCase() + name.substring(1)
    }
}
