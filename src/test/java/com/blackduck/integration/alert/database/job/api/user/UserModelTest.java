package com.blackduck.integration.alert.database.job.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.enumeration.DefaultUserRole;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.persistence.model.UserRoleModel;

class UserModelTest {

    @Test
    void testUserModel() {
        String expectedUserName = "expectedUser";
        String expectedPassword = "expectedPassword";
        String expectedEmail = "expectedEmail";
        Set<String> roleNames = Arrays.stream(DefaultUserRole.values()).map(DefaultUserRole::name).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<UserRoleModel> expectedRoles = roleNames.stream().map(UserRoleModel::of).collect(Collectors.toSet());
        UserModel userModel = UserModel.newUser(expectedUserName, expectedPassword, expectedEmail, AuthenticationType.DATABASE, expectedRoles, true);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedEmail, userModel.getEmailAddress());
        assertEquals(expectedRoles.size(), userModel.getRoles().size());
        assertTrue(userModel.hasRole(DefaultUserRole.ALERT_ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertFalse(userModel.isExpired());
        assertFalse(userModel.isLocked());
        assertFalse(userModel.isPasswordExpired());
        assertTrue(userModel.isEnabled());
        assertFalse(userModel.isExternal());
    }

    @Test
    void testUserModelNullRoles() {
        String expectedUserName = "expectedUser";
        String expectedPassword = "expectedPassword";
        String expectedEmail = "expectedEmail";
        UserModel userModel = UserModel.newUser(expectedUserName, expectedPassword, expectedEmail, AuthenticationType.DATABASE, null, true);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedEmail, userModel.getEmailAddress());
        assertNull(userModel.getRoles());
        assertFalse(userModel.hasRole(DefaultUserRole.ALERT_ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertFalse(userModel.isExpired());
        assertFalse(userModel.isLocked());
        assertFalse(userModel.isPasswordExpired());
        assertTrue(userModel.isEnabled());
        assertFalse(userModel.isExternal());
    }

    @Test
    void testUserModelEmptyRoles() {
        String expectedUserName = "expectedUser";
        String expectedPassword = "expectedPassword";
        String expectedEmail = "expectedEmail";
        Set<UserRoleModel> expectedRoles = new LinkedHashSet<>();
        UserModel userModel = UserModel.newUser(expectedUserName, expectedPassword, expectedEmail, AuthenticationType.DATABASE, expectedRoles, true);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedEmail, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());
        assertFalse(userModel.hasRole(DefaultUserRole.ALERT_ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertFalse(userModel.isExpired());
        assertFalse(userModel.isLocked());
        assertFalse(userModel.isPasswordExpired());
        assertTrue(userModel.isEnabled());
        assertFalse(userModel.isExternal());
    }
}
