package com.palantir.giraffe.file.test.creator;

import java.io.IOException;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for access tests.
 *
 * @author bkeyes
 */
public class AccessTestCreator implements Creator {

    public static final String F_RO_EXISTS = "ro_exists.txt";
    public static final String F_RO_READABLE = "ro_readable.txt";
    public static final String F_RO_WRITEABLE = "ro_writable.txt";
    public static final String F_RO_EXECUTABLE = "ro_executable.sh";
    public static final String F_RO_NONE = "ro_none.txt";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createFile(F_RO_EXISTS);
        script.createFile(F_RO_READABLE, 0444);
        script.createFile(F_RO_WRITEABLE, 0666);
        script.createFile(F_RO_EXECUTABLE, 0755);
        script.createFile(F_RO_NONE, 0000);
    }

}
