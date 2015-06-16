package com.palantir.giraffe.command.test.creator;

import java.io.IOException;
import java.util.Arrays;

import com.palantir.giraffe.test.TestFileCreatorCli;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates a test file directory for execution system tests.
 * <p>
 * Usage:
 * <pre>java CreateTestFiles path/to/dir</pre>
 *
 * @author bkeyes
 */
public class CreateTestFiles {

    private static final Creator[] CREATORS = new Creator[] {
        new ScriptExtractionCreator(),
        new CommandContextCreator()
    };

    public static void main(String[] args) throws IOException {
        TestFileCreatorCli creatorCli = new TestFileCreatorCli(
                CreateTestFiles.class.getSimpleName(),
                Arrays.asList(CREATORS));
        creatorCli.run(args);
    }

    private CreateTestFiles() {
        throw new UnsupportedOperationException();
    }

}
