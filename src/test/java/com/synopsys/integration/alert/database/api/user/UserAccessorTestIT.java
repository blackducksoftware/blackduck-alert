package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;

public class UserAccessorTestIT extends AlertIntegrationTest {

    @Autowired
    private UserAccessor userAccessor;

    @Test
    public void testGetUsers() {
        final String userName_1 = "testUser_1";
        final String password_1 = "testPassword_1";
        userAccessor.addUser(userName_1, password_1);

        final String userName_2 = "testUser_2";
        final String password_2 = "testPassword_2";
        userAccessor.addUser(userName_2, password_2);

        List<UserModel> modelList = userAccessor.getUsers();

        // default admin user will be included
        assertEquals(3, modelList.size());
        userAccessor.deleteUser(userName_1);
        userAccessor.deleteUser(userName_2);
        modelList = userAccessor.getUsers();
        assertEquals(1, modelList.size());
    }

    @Test
    public void testGetUserByUserName() {
        final Optional<UserModel> user = userAccessor.getUser("sysadmin");
        assertTrue(user.isPresent());
    }

    @Test
    public void testGetUserByUserNameNotFound() {
        final Optional<UserModel> user = userAccessor.getUser("anUnknownUser");
        assertFalse(user.isPresent());
    }

    @Test
    public void testAddUser() {
        final String userName = "testUser";
        final String password = "testPassword";
        final UserModel userModel = userAccessor.addUser(userName, password);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertNotNull(userModel.getName());
        assertTrue(userModel.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testUpdateUser() {
        final String userName = "testUser";
        final String password = "testPassword";
        final UserModel userModel = userAccessor.addUser(userName, password);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertNotNull(userModel.getName());
        assertTrue(userModel.getRoles().isEmpty());

        final String another_role = "ANOTHER_ROLE";
        final String admin_role = "ADMIN";
        final Set<String> roles = new LinkedHashSet<>(Arrays.asList(admin_role, another_role));
        final UserModel updatedModel = userAccessor.addOrUpdateUser(UserModel.of(userModel.getName(), userModel.getPassword(), roles), true);
        assertEquals(userModel.getName(), updatedModel.getName());
        assertEquals(userModel.getPassword(), updatedModel.getPassword());
        assertEquals(1, updatedModel.getRoles().size());

        assertFalse(updatedModel.hasRole(another_role));
        assertTrue(updatedModel.hasRole(admin_role));
        userAccessor.deleteUser(userName);
    }

    @Test
    public void testChangeUserPassword() {
        final String userName = "testUser";
        final String password = "testPassword";
        final UserModel userModel = userAccessor.addUser(userName, password);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertNotNull(userModel.getName());
        assertTrue(userModel.getRoles().isEmpty());

        assertTrue(userAccessor.changeUserPassword(userModel.getName(), "new_test_password"));
        final Optional<UserModel> foundModel = userAccessor.getUser(userName);
        assertTrue(foundModel.isPresent());
        if (foundModel.isPresent()) {
            final UserModel updatedModel = foundModel.get();
            assertEquals(userModel.getName(), updatedModel.getName());
            assertNotEquals(userModel.getPassword(), updatedModel.getPassword());
        } else {
            fail();
        }

        userAccessor.deleteUser(userName);

        assertFalse(userAccessor.changeUserPassword("bad_user_name", "new_test_password"));
    }
}
