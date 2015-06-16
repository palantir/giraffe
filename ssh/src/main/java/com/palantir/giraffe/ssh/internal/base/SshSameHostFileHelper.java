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
