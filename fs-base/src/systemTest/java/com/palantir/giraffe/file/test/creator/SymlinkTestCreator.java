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
