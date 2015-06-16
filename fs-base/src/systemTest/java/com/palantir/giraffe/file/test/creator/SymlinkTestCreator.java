package com.palantir.giraffe.file.test.creator;

import java.io.IOException;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for symlink tests.
 *
 * @author bkeyes
 */
public class SymlinkTestCreator implements Creator {

    public static final String F_RO_TARGET = "ro_link_target.txt";
    public static final String F_RO_SYMLINK = "ro_link.txt";
    public static final String F_RO_BROKEN_SYMLINK = "ro_broken_link.txt";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createFile(F_RO_TARGET);
        script.createSymlink(F_RO_SYMLINK, "${PWD}/" + F_RO_TARGET);
        script.createSymlink(F_RO_BROKEN_SYMLINK, "ro_broken_link_target.txt");
    }

}
