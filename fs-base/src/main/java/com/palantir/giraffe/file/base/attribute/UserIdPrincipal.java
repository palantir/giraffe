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

import java.nio.file.attribute.UserPrincipal;

/**
 * A {@link UserPrincipal} that represents users by integer IDs.
 *
 * @author bkeyes
 */
public final class UserIdPrincipal implements UserPrincipal {

    /**
     * Creates a {@code UserIdPrincipal} from a {@code UserPrincipal} with an
     * unknown type.
     *
     * @param user the principal to convert
     *
     * @return a {@code UserIdPrincipal}
     *
     * @throws IllegalArgumentException if the principal is not a
     *         {@code UserIdPrincipal} and does not have a numeric name.
     */
    public static UserIdPrincipal fromUserPrinciple(UserPrincipal user) {
        if (checkNotNull(user, "user must be non-null") instanceof UserIdPrincipal) {
            return (UserIdPrincipal) user;
        } else {
            try {
                int uid = Integer.parseInt(user.getName());
                return new UserIdPrincipal(uid);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("user must have an integer name", e);
            }
        }
    }

    private final int uid;

    public UserIdPrincipal(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    /**
     * Returns a string representation of this user's ID.
     */
    @Override
    public String getName() {
        return Integer.toString(uid);
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserIdPrincipal)) {
            return false;
        } else {
            return uid == ((UserIdPrincipal) obj).uid;
        }
    }

}
