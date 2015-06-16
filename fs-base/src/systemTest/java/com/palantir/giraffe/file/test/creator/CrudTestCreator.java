package com.palantir.giraffe.file.test.creator;

import java.io.IOException;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for CRUD tests.
 *
 * @author bkeyes
 */
public class CrudTestCreator implements Creator {

    public static final String F_RO_READ = "ro_crud_read.txt";

    public static final String READ_DATA = "Can you read this now?";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createFile(F_RO_READ, READ_DATA);
    }

}
