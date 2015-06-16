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
package com.palantir.giraffe.ssh.internal.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.palantir.giraffe.command.CommandResult;
import com.palantir.giraffe.file.base.CopyFlags;

final class SshSameHostFileHelper {

    public static void copyFile(BaseSshPath<?> source, BaseSshPath<?> target,
            CopyFlags flags) throws IOException {
        List<Object> args = new ArrayList<>();
        if (flags.replaceExisting) {
            args.add("-f");
        } else {
            args.add("-n");
        }
        if (flags.followLinks) {
            args.add("-L");
        } else {
            args.add("-P");
        }
        if (flags.copyAttributes) {
            args.add("-p");
        }
        args.add(source);
        args.add(target);

        CommandResult result = source.getFileSystem().execute("cp", args);
        if (result.getExitStatus() != 0) {
            // TODO(bkeyes): throw FileAlreadyExistsException
            // TODO(bkeyes): throw DirectoryNotEmptyException
            throw commandError("cp command failed", result);
        }
    }


    // TODO(bkeyes): technically, this should probably fail if it moves a path
    // between partitions on the remote host. Is this actually a problem?
    public static void movePath(BaseSshPath<?> source, BaseSshPath<?> target,
            CopyFlags flags) throws IOException {
        List<Object> args = new ArrayList<>();
        if (flags.replaceExisting) {
            args.add("-f");
        } else {
            args.add("-n");
        }
        args.add(source);
        args.add(target);

        CommandResult result = source.getFileSystem().execute("mv", args);
        if (result.getExitStatus() != 0) {
            // TODO(bkeyes): throw FileAlreadyExistsException
            // TODO(bkeyes): throw DirectoryNotEmptyException
            throw commandError("mv command failed", result);
        }
    }

    public static void deleteRecursive(BaseSshPath<?> target)
            throws IOException {
        CommandResult result = target.getFileSystem().execute("rm", "-rf", target);
        if (result.getExitStatus() != 0) {
            throw commandError("rm command failed", result);
        }
    }

    public static void copyRecursive(BaseSshPath<?> source, BaseSshPath<?> target)
            throws IOException {
        CommandResult result = target.getFileSystem().execute("cp", "-r", source, target);
        if (result.getExitStatus() != 0) {
            throw commandError("cp command failed", result);
        }
    }

    public static void changePermissionsRecursive(BaseSshPath<?> target, String mode)
            throws IOException {
        CommandResult result = target.getFileSystem().execute("chmod", "-R", mode, target);
        if (result.getExitStatus() != 0) {
            throw commandError("chmod command failed", result);
        }
    }

    private static IOException commandError(String msg, CommandResult result) throws IOException {
        throw new IOException(msg + String.format(" [exit status = %s, output = %s]",
                result.getExitStatus(),
                result.getStdErr()));
    }

    private SshSameHostFileHelper() {
        throw new UnsupportedOperationException();
    }
}
