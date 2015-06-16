package com.palantir.giraffe.gradle.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.JavaExec;

class SystemTestFilesPlugin implements Plugin<Project> {

    void apply(Project project) {
        def creators = project.container(TestFileCreator, { name ->
            return new TestFileCreator(name, project)
        })
        creators.all { c -> addCreatorTasks(project, c) }

        project.extensions.create("systemTestFiles", SystemTestFilesExtension, creators)
        project.systemTestFiles.filesDir = new File(project.buildDir, 'system-test-files')
    }

    static void addCreatorTasks(Project project, TestFileCreator c) {
        def createScriptTask = project.task("generate${c.taskName}CreatorScript", type: JavaExec) {
            ext.scriptPath = "${-> project.systemTestFiles.filesDir}/${c.name}-creator.sh"
            outputs.file scriptPath

            // lazily set both the classpath and main class
            classpath { c.classpath }
            doFirst {
                main = c.main
            }

            args scriptPath
        }

        def createFilesTask = project.task("create${c.taskName}TestFiles", type: Exec) {
            ext.outputDir = "${-> project.systemTestFiles.filesDir}/${c.name}"
            inputs.file createScriptTask
            outputs.upToDateWhen { 
                project.file(outputDir).exists() 
            }

            group = 'Verification'
            description = "Creates '${c.name}' test files."

            commandLine createScriptTask.scriptPath, outputDir
        }

        project.tasks['test'].dependsOn createFilesTask
    }
}
