package com.palantir.giraffe.gradle.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.bundling.Jar;

class SystemTestPlugin implements Plugin<Project> {

    private static final String NAME = 'systemTest'
    private static final String CREATOR_NAME = 'systemTestCreator'

    void apply(Project project) {
        project.extensions.create(NAME, SystemTestExtension)

        // add system test source set and configurations
        project.sourceSets.create(NAME)
        project.configurations.create(NAME) {
            extendsFrom project.configurations[NAME + 'Implementation']
        }
        project.configurations.create(CREATOR_NAME)

        // TODO(bkeyes): don't assume Eclipse exists
        project.eclipse.classpath {
            plusConfigurations += [project.configurations[NAME]]
        }

        def deps = project.dependencies;
        deps.add(NAME + 'Implementation', deps.project(path: ':giraffe-test-util'))

        // define the main test jar task
        def testJar = project.task(NAME + 'Jar', type: Jar) {
            appendix = 'system-test'
            from project.sourceSets[NAME].output
        }
        project.artifacts.add(NAME, testJar)

        addCreatorTasks(project)
    }

    static void addCreatorTasks(Project project) {
        // define the creator jar task
        def creatorJar = project.task('creatorJar', type: Jar) {
            appendix = 'creator'

            // add creator classes from this project
            from(project.sourceSets[NAME].output) {
                include '**/creator/**'
            }

            // lazily add test-util dependency classes
            // TODO(bkeyes): get this from a configuration?
            from({ project.project(':giraffe-test-util').sourceSets.main.output }) {
                exclude '**/runner/**'
            }

            manifest {
                attributes 'Main-Class': "${-> getCreatorClass(project)}"
            }
        }
        project.artifacts.add(NAME + 'Creator', creatorJar)

        // run the creator jar to verify it has no dependencies
        def creatorCheck = project.task('checkCreatorJarHasNoDependencies', type: JavaExec) {
            ext.outputScript = new File(temporaryDir, 'creator-script.sh')

            inputs.files creatorJar
            outputs.file outputScript

            classpath creatorJar

            // defer configuration until runtime
            mainClass = project.provider({ getCreatorClass(project) })
            args outputScript
        }
        project.tasks['check'].dependsOn(creatorCheck)
    }

    static String getCreatorClass(Project project) {
        return project.extensions.getByName(NAME).creatorClass
    }
}
