package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.UserModel;
import com.synopsys.integration.alert.common.rest.model.UserRoleModel;
import com.synopsys.integration.alert.database.user.UserRole;

public class UserModelTest {

    @Test
    public void testUserModel() {
        final String expectedUserName = "expectedUser";
        final String expectedPassword = "expectedPassword";
        final String expectedEmail = "expectedEmail";
        final Set<String> roleNames = new LinkedHashSet<>(Arrays.asList(UserRole.values()).stream().map(UserRole::name).collect(Collectors.toList()));
        final Set<UserRoleModel> expectedRoles = roleNames.stream().map(UserRoleModel::of).collect(Collectors.toSet());
        final UserModel userModel = UserModel.of(expectedUserName, expectedPassword, expectedEmail, expectedRoles);

        assertEquals(expectedUserName, userModel.getName());
        assertEquals(expectedPassword, userModel.getPassword());
        assertEquals(expectedEmail, userModel.getEmailAddress());
        assertEquals(expectedRoles.size(), userModel.getRoles().size());
        assertTrue(userModel.hasRole(UserRole.ALERT_ADMIN.name()));
        assertFalse(userModel.hasRole("UNKNOWN_ROLE"));
        assertFalse(userModel.isExpired());
        assertFalse(userModel.isLocked());
        assertFalse(userModel.isPasswordExpired());
        assertTrue(userModel.isEnabled());
    }
}
