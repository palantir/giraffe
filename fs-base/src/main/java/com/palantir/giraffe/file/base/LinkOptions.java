package com.palantir.giraffe.file.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.LinkOption;

/**
 * Utilities for working with {@link LinkOption}s.
 *
 * @author bkeyes
 */
public final class LinkOptions {

    public static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        for (LinkOption option : options) {
            if (checkNotNull(option) == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
            }
        }
        return followLinks;
    }

    private LinkOptions() {
        throw new UnsupportedOperationException();
    }

    public static LinkOption[] toArray(boolean followLinks) {
        if (followLinks) {
            return new LinkOption[0];
        } else {
            return new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
        }
    }
}
