package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Container for standard {@link CopyOption}s.
 * <p>
 * This class may be extended by file system implementations that add custom
 * options. Use {@link #setStandardOption(CopyFlags, CopyOption)} to set
 * standard options in custom subclasses.
 *
 * @author bkeyes
 *
 * @see StandardCopyOption
 * @see LinkOption
 */
public class CopyFlags {

    public boolean replaceExisting = false;
    public boolean copyAttributes = false;
    public boolean atomicMove = false;
    public boolean followLinks = true;

    public static CopyFlags fromOptions(CopyOption... options) {
        return fromOptions(Arrays.asList(options));
    }

    public static CopyFlags fromOptions(Iterable<? extends CopyOption> options) {
        CopyFlags flags = new CopyFlags();
        for (CopyOption option : options) {
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
    protected static boolean setStandardOption(CopyFlags flags, CopyOption option) {
        if (checkNotNull(option) instanceof StandardCopyOption) {
            setStandardCopyOption(flags, (StandardCopyOption) option);
            return true;
        } else if (option == LinkOption.NOFOLLOW_LINKS) {
            flags.followLinks = false;
            return true;
        } else {
            return false;
        }
    }

    private static void setStandardCopyOption(CopyFlags flags, StandardCopyOption option) {
        switch (option) {
            case ATOMIC_MOVE:
                flags.atomicMove = true;
                break;
            case COPY_ATTRIBUTES:
                flags.copyAttributes = true;
                break;
            case REPLACE_EXISTING:
                flags.replaceExisting = true;
                break;
        }
    }
}
