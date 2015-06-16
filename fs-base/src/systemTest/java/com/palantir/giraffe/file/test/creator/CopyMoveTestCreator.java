/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.giraffe.file.test.creator;

import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Set;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for copy and move tests.
 *
 * @author bkeyes
 */
public class CopyMoveTestCreator implements Creator {

    public static final String F_RO_FILE = "ro_cpmv_file.txt";
    public static final String F_RO_EMPTY_DIR = "ro_cpmv_empty_dir";
    public static final String F_RO_EXEC_FILE = "ro_cpmv_exec_file.sh";

    public static final String FILE_DATA = "Copy and/or move me!";
    public static final String EXEC_FILE_DATA = "#!/bin/bash\necho \"Hello World\"\n";
    public static final Set<PosixFilePermission> EXEC_FILE_PERMS = Collections.unmodifiableSet(
            PosixFilePermissions.fromString("rwxr-xr-x"));

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createFile(F_RO_FILE, FILE_DATA);
        script.createDir(F_RO_EMPTY_DIR);
        script.createFile(F_RO_EXEC_FILE, EXEC_FILE_DATA, 0755);
    }

}
