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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class UserAccessorTestIT extends AlertIntegrationTest {

    @Autowired
    private DefaultUserAccessor userAccessor;

    @Test
    public void testGetUsers() {
        String userName_1 = "testUser_1";
        String password_1 = "testPassword_1";
        String email_1 = "testEmail_1";
        userAccessor.addUser(userName_1, password_1, email_1);

        String userName_2 = "testUser_2";
        String password_2 = "testPassword_2";
        String email_2 = "testEmail_2";
        userAccessor.addUser(userName_2, password_2, email_2);

        List<UserModel> modelList = userAccessor.getUsers();

        // default admin, jobmanager, alertuser user will be included
        assertEquals(5, modelList.size());
        userAccessor.deleteUser(userName_1);
        userAccessor.deleteUser(userName_2);
        modelList = userAccessor.getUsers();
        assertEquals(3, modelList.size());
    }

    @Test
    public void testGetUserByUserName() {
        Optional<UserModel> user = userAccessor.getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER);
        assertTrue(user.isPresent());
    }

    @Test
    public void testGetUserByUserNameNotFound() {
        Optional<UserModel> user = userAccessor.getUser("anUnknownUser");
        assertFalse(user.isPresent());
    }

    @Test
    public void testAddUser() {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        userAccessor.deleteUser(userName);
    }

    @Test
    public void testUpdateUser() {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        String another_role = "ANOTHER_ROLE";
        String admin_role = AlertIntegrationTest.ROLE_ALERT_ADMIN;
        Set<String> roleNames = new LinkedHashSet<>(Arrays.asList(admin_role, another_role));
        Set<UserRoleModel> roles = roleNames.stream().map(UserRoleModel::of).collect(Collectors.toSet());
        UserModel updatedModel = userAccessor.addOrUpdateUser(UserModel.of(userModel.getName(), userModel.getPassword(), userModel.getEmailAddress(), roles, true), true);
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
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        assertTrue(userAccessor.changeUserPassword(userModel.getName(), "new_test_password"));
        Optional<UserModel> foundModel = userAccessor.getUser(userName);
        assertTrue(foundModel.isPresent());
        if (foundModel.isPresent()) {
            UserModel updatedModel = foundModel.get();
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
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        assertTrue(userAccessor.changeUserEmailAddress(userModel.getName(), "new_test_email"));
        Optional<UserModel> foundModel = userAccessor.getUser(userName);
        assertTrue(foundModel.isPresent());
        if (foundModel.isPresent()) {
            UserModel updatedModel = foundModel.get();
            assertEquals(userModel.getName(), updatedModel.getName());
            assertNotEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());
        } else {
            fail();
        }

        userAccessor.deleteUser(userName);

        assertFalse(userAccessor.changeUserEmailAddress("bad_user_name", "new_test_email"));
    }
}
