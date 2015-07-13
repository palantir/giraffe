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
import java.util.Arrays;

import com.palantir.giraffe.test.TestFileCreatorCli;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates a test file directory for file system tests.
 * <p>
 * Usage:
 * <pre>java CreateTestFiles path/to/dir</pre>
 *
 * @author bkeyes
 */
public class CreateTestFiles {

    private static final Creator[] CREATORS = new Creator[] {
        new AccessTestCreator(),
        new AttributeTestCreator(),
        new DirectoryTestCreator(),
        new CrudTestCreator(),
        new SymlinkTestCreator(),
        new CopyMoveTestCreator()
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
