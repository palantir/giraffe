package com.palantir.giraffe.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

/**
 * Generates a shell script that creates test files for file and execution
 * system tests.
 *
 * @author bkeyes
 */
public class TestFileCreatorCli {

    /**
     * Adds directives to a test file creation script.
     */
    public interface Creator {
        void createScript(ScriptWriter script) throws IOException;
    }

    private final String name;
    private final List<Creator> creators;

    public TestFileCreatorCli(String name, List<Creator> creators) {
        this.name = name;
        this.creators = creators;
    }

    public void run(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("ERROR: missing output path");
            System.err.println("usage: java " + name + " path/to/output/script.sh");
            System.exit(1);
        }

        Path output = Paths.get(args[0]);
        Files.deleteIfExists(output);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (ScriptWriter script = new ScriptWriter(output.toFile(), StandardCharsets.UTF_8)) {
            script.println("#!/bin/sh");
            script.println();

            script.println("if [ $# -ne 1 ]; then");
            script.println("    echo 'missing output directory argument' 1>&2");
            script.println("    exit 1");
            script.println("fi");
            script.println();

            script.println("DIR=$1");
            script.println("rm -rf $DIR && mkdir $DIR && cd $DIR");

            for (Creator creator : creators) {
                script.println();
                creator.createScript(script);
            }
        }

        Files.setPosixFilePermissions(output, PosixFilePermissions.fromString("rwxr-xr-x"));
    }

}
