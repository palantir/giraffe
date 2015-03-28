package com.palantir.giraffe.file.test.creator;

import java.io.IOException;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for directory listing tests.
 *
 * @author bkeyes
 */
public class DirectoryTestCreator implements Creator {

    public static final String F_RO_DIRECTORY = "ro_directory";
    public static final String F_RO_TEXT = "ro_text.txt";
    public static final String F_RO_LOG = "ro_error.log";
    public static final String F_RO_SYMLINK = "ro_symlink.txt";
    public static final String F_RO_SUBDIR = "ro_subdir";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createDir(F_RO_DIRECTORY);

        String dirPrefix = F_RO_DIRECTORY + "/";
        script.createFile(dirPrefix + F_RO_TEXT);
        script.createFile(dirPrefix + F_RO_LOG);
        script.createSymlink(dirPrefix + F_RO_SYMLINK, F_RO_TEXT);

        script.createDir(dirPrefix + F_RO_SUBDIR);
    }

}
