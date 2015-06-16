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
