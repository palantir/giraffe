package com.palantir.giraffe.file.base.attribute;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.attribute.GroupPrincipal;

/**
 * A {@link GroupPrincipal} that represents users by integer IDs.
 *
 * @author bkeyes
 */
public final class GroupIdPrincipal implements GroupPrincipal {

    /**
     * Creates a {@code GroupIdPrincipal} from a {@code GroupPrincipal} with an
     * unknown type.
     *
     * @param group the principal to convert
     *
     * @return a {@code GroupIdPrincipal}
     *
     * @throws IllegalArgumentException if the principal is not a
     *         {@code GroupIdPrincipal} and does not have a numeric name.
     */
    public static GroupIdPrincipal fromGroupPrinciple(GroupPrincipal group) {
        if (checkNotNull(group, "group must be non-null") instanceof GroupIdPrincipal) {
            return (GroupIdPrincipal) group;
        } else {
            try {
                int gid = Integer.parseInt(group.getName());
                return new GroupIdPrincipal(gid);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("group must have an integer name", e);
            }
        }
    }

    private final int gid;

    public GroupIdPrincipal(int gid) {
        this.gid = gid;
    }

    public int getGid() {
        return gid;
    }

    /**
     * Returns a string representation of this group's ID.
     */
    @Override
    public String getName() {
        return Integer.toString(gid);
    }

    @Override
    public int hashCode() {
        return gid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupIdPrincipal)) {
            return false;
        } else {
            return gid == ((GroupIdPrincipal) obj).gid;
        }
    }

}
