package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

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
    }
}
