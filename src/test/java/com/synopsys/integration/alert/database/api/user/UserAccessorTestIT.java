package com.synopsys.integration.alert.database.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.data.model.UserModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class UserAccessorTestIT extends AlertIntegrationTest {

    @Autowired
    private UserAccessor userAccessor;

    @Test
    public void testGetUsers() {
        final String userName_1 = "testUser_1";
        final String password_1 = "testPassword_1";
        final String email_1 = "testEmail_1";
        userAccessor.addUser(userName_1, password_1, email_1);

        final String userName_2 = "testUser_2";
        final String password_2 = "testPassword_2";
        final String email_2 = "testEmail_2";
        userAccessor.addUser(userName_2, password_2, email_2);

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
        final Optional<UserModel> user = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER);
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
        final String email = "testEmail";
        final UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testUpdateUser() {
        final String userName = "testUser";
        final String password = "testPassword";
        final String email = "testEmail";
        final UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        final String another_role = "ANOTHER_ROLE";
        final String admin_role = "ADMIN";
        final Set<String> roles = new LinkedHashSet<>(Arrays.asList(admin_role, another_role));
        final UserModel updatedModel = userAccessor.addOrUpdateUser(UserModel.of(userModel.getName(), userModel.getPassword(), userModel.getEmailAddress(), roles), true);
        assertEquals(userModel.getName(), updatedModel.getName());
        assertEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());
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
        final String email = "testEmail";
        final UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
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

    @Test
    public void testChangeUserEmailAddress() {
        final String userName = "testUser";
        final String password = "testPassword";
        final String email = "testEmail";
        final UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        assertTrue(userAccessor.changeUserEmailAddress(userModel.getName(), "new_test_email"));
        final Optional<UserModel> foundModel = userAccessor.getUser(userName);
        assertTrue(foundModel.isPresent());
        if (foundModel.isPresent()) {
            final UserModel updatedModel = foundModel.get();
            assertEquals(userModel.getName(), updatedModel.getName());
            assertNotEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());
        } else {
            fail();
        }

        userAccessor.deleteUser(userName);

        assertFalse(userAccessor.changeUserEmailAddress("bad_user_name", "new_test_email"));
    }
}
