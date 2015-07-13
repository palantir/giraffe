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
package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Container for standard {@link OpenOption}s.
 * <p>
 * This class may be extended by file system implementations that add custom
 * options. Use {@link #setStandardOption(OpenFlags, OpenOption)} to set
 * standard options in custom subclasses.
 *
 * @author bkeyes
 *
 * @see StandardOpenOption
 * @see LinkOption
 */
public class OpenFlags {

    public boolean read = false;
    public boolean write = false;
    public boolean append = false;
    public boolean create = false;
    public boolean createNew = false;
    public boolean truncateExisting = false;
    public boolean deleteOnClose = false;
    public boolean sparse = false;
    public boolean dsync = false;
    public boolean sync = false;
    public boolean followLinks = true;

    /**
     * Converts the specified options to flags, sets missing implied flags, and
     * checks for invalid combinations.
     *
     * @param options the options to convert and validate
     *
     * @throws IllegalArgumentException if the iterable contains an invalid
     *         combination of options
     *
     * @see java.nio.file.Files#newByteChannel Files.newByteChannel
     */
    public static OpenFlags validateFromOptions(Iterable<? extends OpenOption> options) {
        OpenFlags flags = fromOptions(options);
        if (!flags.read && !flags.write) {
            if (flags.append) {
                flags.write = true;
            } else {
                flags.read = true;
            }
        }

        if (flags.append && flags.read) {
            throw new IllegalArgumentException("APPEND + READ not allowed");
        }
        if (flags.append && flags.truncateExisting) {
            throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
        }
        return flags;
    }

    /**
     * Converts the specified options to flags.
     *
     * @param options the options to convert
     *
     * @throws UnsupportedOperationException if an unsupported option is
     *         specified
     */
    public static OpenFlags fromOptions(OpenOption... options) {
        return fromOptions(Arrays.asList(options));
    }

    /**
     * Converts the specified options to flags.
     *
     * @param options the options to convert
     *
     * @throws UnsupportedOperationException if an unsupported option is
     *         specified
     */
    public static OpenFlags fromOptions(Iterable<? extends OpenOption> options) {
        OpenFlags flags = new OpenFlags();
        for (OpenOption option : options) {
            if (!setStandardOption(flags, option)) {
                throw new UnsupportedOperationException("unknown option: " + option);
            }
        }
        return flags;
    }

    /**
     * Sets a standard option in {@code flags}.
     *
     * @param flags the flags object in which to set the option
     * @param option the standard option to set
     *
     * @return {@code true} if the option was recognized and set or
     *         {@code false} if the option was non-standard.
     */
    protected static boolean setStandardOption(OpenFlags flags, OpenOption option) {
        if (checkNotNull(option) instanceof StandardOpenOption) {
            setStandardOpenOption(flags, (StandardOpenOption) option);
            return true;
        } else if (option == LinkOption.NOFOLLOW_LINKS) {
            flags.followLinks = false;
            return true;
        } else {
            return false;
        }
    }

    private static void setStandardOpenOption(OpenFlags flags, StandardOpenOption option) {
        switch (option) {
            case READ:
                flags.read = true;
                break;
            case WRITE:
                flags.write = true;
                break;
            case APPEND:
                flags.append = true;
                break;
            case CREATE:
                flags.create = true;
                break;
            case CREATE_NEW:
                flags.createNew = true;
                break;
            case TRUNCATE_EXISTING:
                flags.truncateExisting = true;
                break;
            case DELETE_ON_CLOSE:
                flags.deleteOnClose = true;
                break;
            case SPARSE:
                flags.sparse = true;
                break;
            case DSYNC:
                flags.dsync = true;
                break;
            case SYNC:
                flags.sync = true;
                break;
        }
    }
}
