package com.palantir.giraffe.file.base.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

/**
 * Tests basic functionality of {@link ChmodFilePermissions}.
 *
 * @author bkeyes
 */
public class ChmodFilePermissionsTest {

    @Test
    public void toOwnerBits() {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE);

        int bits = ChmodFilePermissions.toBits(permissions);
        assertThat(bits, equalsBits(0600));
    }

    @Test
    public void toGroupBits() {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE);

        int bits = ChmodFilePermissions.toBits(permissions);
        assertThat(bits, equalsBits(0050));
    }

    @Test
    public void toOtherBits() {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OTHERS_WRITE,
                PosixFilePermission.OTHERS_EXECUTE);

        int bits = ChmodFilePermissions.toBits(permissions);
        assertThat(bits, equalsBits(0003));
    }

    @Test
    public void toCombinedBits() {
        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.OTHERS_READ);

        int bits = ChmodFilePermissions.toBits(permissions);
        assertThat(bits, equalsBits(0724));
    }

    @Test
    public void toOwnerPermissions() {
        Set<PosixFilePermission> permissions = ChmodFilePermissions.toPermissions(0600);
        assertEquals("incorrect permissions",
                EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                permissions);
    }

    @Test
    public void toGroupPermissions() {
        Set<PosixFilePermission> permissions = ChmodFilePermissions.toPermissions(0050);
        assertEquals("incorrect permissions",
                EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE),
                permissions);
    }

    @Test
    public void toOtherPermissions() {
        Set<PosixFilePermission> permissions = ChmodFilePermissions.toPermissions(0003);
        assertEquals("incorrect permissions",
                EnumSet.of(PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_EXECUTE),
                permissions);
    }

    @Test
    public void toCombinedPermissions() {
        Set<PosixFilePermission> permissions = ChmodFilePermissions.toPermissions(0724);
        assertEquals("incorrect permissions",
                EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.GROUP_WRITE,
                        PosixFilePermission.OTHERS_READ),
                permissions);
    }

    @Test
    public void toOwnerMode() {
        String mode = ChmodFilePermissions.toMode(PermissionChange.ADD,
                EnumSet.of(PosixFilePermission.OWNER_EXECUTE));
        assertEquals("incorrect mode", "u+x", mode);
    }

    @Test
    public void toGroupMode() {
        String mode = ChmodFilePermissions.toMode(PermissionChange.REMOVE,
                EnumSet.of(PosixFilePermission.GROUP_WRITE));
        assertEquals("incorrect mode", "g-w", mode);
    }

    @Test
    public void toOtherMode() {
        String mode = ChmodFilePermissions.toMode(PermissionChange.SET,
                EnumSet.of(PosixFilePermission.OTHERS_READ));
        assertEquals("incorrect mode", "o=r", mode);
    }

    @Test
    public void toCombinedMode() {
        String mode = ChmodFilePermissions.toMode(PermissionChange.ADD,
                EnumSet.of(
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.GROUP_WRITE,
                        PosixFilePermission.GROUP_EXECUTE,
                        PosixFilePermission.OTHERS_EXECUTE));
        assertEquals("incorrect mode", "u+x,g+wx,o+x", mode);
    }

    private static Matcher<Integer> equalsBits(final int bits) {
        return new TypeSafeDiagnosingMatcher<Integer>() {
            @Override
            public void describeTo(Description d) {
                d.appendText(String.format("bits 0%o", bits));
            }

            @Override
            protected boolean matchesSafely(Integer item, Description d) {
                d.appendText(String.format("was bits 0%o", item));
                return item == bits;
            }
        };
    }

}
