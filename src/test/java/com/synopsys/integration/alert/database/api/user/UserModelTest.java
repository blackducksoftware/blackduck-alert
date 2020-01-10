package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;

public class UserModelTest {

    @Test
    public void testUserModel() {
        String expectedUserName = "expectedUser";
        String expectedPassword = "expectedPassword";
        String expectedEmail = "expectedEmail";
        Set<String> roleNames = new LinkedHashSet<>(Arrays.asList(DefaultUserRole.values()).stream().map(DefaultUserRole::name).collect(Collectors.toList()));
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
    public void testUserModelNullRoles() {
        String expectedUserName = "expectedUser";
        String expectedPassword = "expectedPassword";
        String expectedEmail = "expectedEmail";
        Set<String> roleNames = null;
        Set<UserRoleModel> expectedRoles = null;
        UserModel userModel = UserModel.newUser(expectedUserName, expectedPassword, expectedEmail, AuthenticationType.DATABASE, expectedRoles, true);

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
    public void testUserModelEmptyRoles() {
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
