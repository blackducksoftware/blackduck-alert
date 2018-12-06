package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.synopsys.integration.alert.database.user.UserEntity;

public class UserModelTest {

    @Test
    public void testUserModel() {
        final String expectedUserName = "expectedUser";
        final String expectedPassword = "expectedPassword";
        final Set<String> expectedRoles = new LinkedHashSet<>(Arrays.asList(UserRole.values()).stream().map(UserRole::name).collect(Collectors.toList()));
        final UserModel userModel = UserModel.of(expectedUserName, expectedPassword, expectedRoles);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedRoles.size(), userModel.getRoles().size());
        assertTrue(userModel.hasRole(UserRole.ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertFalse(userModel.isExpired());
        assertFalse(userModel.isLocked());
        assertFalse(userModel.isPasswordExpired());
        assertTrue(userModel.isEnabled());
    }

    @Test
    public void testUserModelWithEntity() {
        final String expectedUserName = "expectedUser";
        final String expectedPassword = "expectedPassword";
        final Set<String> expectedRoles = new LinkedHashSet<>(Arrays.asList(UserRole.values()).stream().map(UserRole::name).collect(Collectors.toList()));
        final boolean expired = true;
        final boolean locked = true;
        final boolean passwordExpired = true;
        final boolean enabled = false;
        final UserEntity entity = new UserEntity(expectedUserName, expectedPassword, expired, locked, passwordExpired, enabled);
        final UserModel userModel = UserModel.of(entity, expectedRoles);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedRoles.size(), userModel.getRoles().size());
        assertTrue(userModel.hasRole(UserRole.ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertTrue(userModel.isExpired());
        assertTrue(userModel.isLocked());
        assertTrue(userModel.isPasswordExpired());
        assertFalse(userModel.isEnabled());
    }
}
