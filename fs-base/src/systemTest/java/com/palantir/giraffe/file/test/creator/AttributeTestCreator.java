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
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Creates test files for attribute tests.
 *
 * @author bkeyes
 */
public class AttributeTestCreator implements Creator {

    public static final String F_RO_MODIFIED_TIME = "ro_modified_time.txt";
    public static final String F_RO_SIZE = "ro_size.txt";
    public static final String F_RO_PERMISSIONS = "ro_permissions.sh";

    public static final String F_RW_MODIFIED_TIME = "rw_modified_time.txt";
    public static final String F_RW_PERMISSIONS = "rw_permissions.sh";

    public static final int SIZE = 314;
    public static final FileTime MODIFIED_TIME;
    public static final Set<PosixFilePermission> PERMISSIONS;
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2012, 12, 12);
        MODIFIED_TIME = FileTime.fromMillis(calendar.getTimeInMillis());

        PERMISSIONS = Collections.unmodifiableSet(PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        script.createFile(F_RO_MODIFIED_TIME);
        script.setModifiedTime(F_RO_MODIFIED_TIME, new Date(MODIFIED_TIME.toMillis()));

        script.printf("dd if=/dev/zero bs=%d count=1 2>/dev/null > %s%n", SIZE, F_RO_SIZE);

        script.createFile(F_RO_PERMISSIONS, 0755);

        script.createFile(F_RW_MODIFIED_TIME);
        script.setModifiedTime(F_RW_MODIFIED_TIME, new Date(MODIFIED_TIME.toMillis()));

        script.createFile(F_RW_PERMISSIONS);
    }

}
