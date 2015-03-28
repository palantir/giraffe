package com.palantir.giraffe.command.test.creator;

import java.io.IOException;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for command context tests.
 *
 * @author bkeyes
 */
public class CommandContextCreator implements Creator {

    public static final String DIR = "workingDir";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createDir(DIR);
    }

}
