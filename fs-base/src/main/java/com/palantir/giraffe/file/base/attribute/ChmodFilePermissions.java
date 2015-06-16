package com.palantir.giraffe.file.base.attribute;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

/**
 * Converts {@link PosixFilePermission}s to and from representations used by the
 * {@code chmod} command and system call.
 *
 * @author bkeyes
 */
public final class ChmodFilePermissions {

    private static final int R_BIT = 0x4;
    private static final int W_BIT = 0x2;
    private static final int E_BIT = 0x1;

    private static final int U_SHIFT = 6;
    private static final int G_SHIFT = 3;
    private static final int O_SHIFT = 0;

    private static final int MASK = 0x7;

    private static final ImmutableMap<PosixFilePermission, Integer> permissionsBitMap;
    static {
        ImmutableMap.Builder<PosixFilePermission, Integer> builder = ImmutableMap.builder();
        builder.put(PosixFilePermission.OWNER_READ, R_BIT << U_SHIFT);
        builder.put(PosixFilePermission.OWNER_WRITE, W_BIT << U_SHIFT);
        builder.put(PosixFilePermission.OWNER_EXECUTE, E_BIT << U_SHIFT);
        builder.put(PosixFilePermission.GROUP_READ, R_BIT << G_SHIFT);
        builder.put(PosixFilePermission.GROUP_WRITE, W_BIT << G_SHIFT);
        builder.put(PosixFilePermission.GROUP_EXECUTE, E_BIT << G_SHIFT);
        builder.put(PosixFilePermission.OTHERS_READ, R_BIT << O_SHIFT);
        builder.put(PosixFilePermission.OTHERS_WRITE, W_BIT << O_SHIFT);
        builder.put(PosixFilePermission.OTHERS_EXECUTE, E_BIT << O_SHIFT);
        permissionsBitMap = builder.build();
    }

    /**
     * Converts the specified bit field representation into a
     * {@code PosixFilePermission} set. Unknown bits are ignored.
     *
     * @param bits the bit field representation of the permissions. Octal
     *        notation is traditionally used when specifying the bit field
     *        directly.
     */
    public static Set<PosixFilePermission> toPermissions(int bits) {
        EnumSet<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
        for (Entry<PosixFilePermission, Integer> e : permissionsBitMap.entrySet()) {
            if ((bits & e.getValue()) != 0) {
                permissions.add(e.getKey());
            }
        }
        return permissions;
    }

    /**
     * Converts the specified {@code PosixFilePermission} set into a bit field
     * representation.
     *
     * @param permissions the set of permissions
     */
    public static int toBits(Set<PosixFilePermission> permissions) {
        int bits = 0;
        for (PosixFilePermission perm : permissions) {
            bits |= permissionsBitMap.get(perm);
        }
        return bits;
    }

    /**
     * Converts the specified {@code PosixFilePermission} set and change type
     * into a {@code chmod} mode string.
     *
     * @param change the type of change
     * @param permissions the permissions to change
     *
     * @return a mode string representing the specified permissions change
     *
     * @throws IllegalArgumentException if {@code permissions} is empty and
     *         {@code change} is not {@code PermissionChange.SET}
     */
    public static String toMode(PermissionChange change, Set<PosixFilePermission> permissions) {
        checkArgument(!permissions.isEmpty() || change == PermissionChange.SET,
                "permission set is empty but change type is not SET");

        StringBuilder modes = new StringBuilder();
        int bits = toBits(permissions);

        int ubits = (bits >> U_SHIFT) & MASK;
        if (ubits != 0) {
            writeMode(modes.append("u").append(change.getOperator()), ubits);
        }

        int gbits = (bits >> G_SHIFT) & MASK;
        if (gbits != 0) {
            if (modes.length() > 0) {
                modes.append(',');
            }
            writeMode(modes.append("g").append(change.getOperator()), gbits);
        }

        int obits = (bits >> O_SHIFT) & MASK;
        if (obits != 0) {
            if (modes.length() > 0) {
                modes.append(',');
            }
            writeMode(modes.append("o").append(change.getOperator()), obits);
        }

        return modes.toString();
    }

    private static void writeMode(StringBuilder mode, int bits) {
        if ((bits & R_BIT) != 0) {
            mode.append('r');
        }
        if ((bits & W_BIT) != 0) {
            mode.append('w');
        }
        if ((bits & E_BIT) != 0) {
            mode.append('x');
        }
    }

    private ChmodFilePermissions() {
        throw new UnsupportedOperationException();
    }

}
