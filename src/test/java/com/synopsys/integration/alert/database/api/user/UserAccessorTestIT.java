package com.synopsys.integration.alert.database.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Transactional
@AlertIntegrationTest
public class UserAccessorTestIT {
    @Autowired
    private DefaultUserAccessor userAccessor;

    @BeforeEach
    public void cleanUpUsers() {
        List<UserModel> userModels = userAccessor.getUsers();

        for (UserModel user : userModels) {
            try {
                userAccessor.deleteUser(user.getId());
            } catch (AlertForbiddenOperationException ex) {
                // this exception is thrown if we delete reserved users.
            }
        }
    }

    @Test
    public void testGetUsers() throws AlertForbiddenOperationException, AlertConfigurationException {
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
        Optional<UserModel> user = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(user.isPresent());
    }

    @Test
    public void testGetUserByUserNameNotFound() {
        Optional<UserModel> user = userAccessor.getUser("anUnknownUser");
        assertFalse(user.isPresent());
    }

    @Test
    public void testAddUser() throws AlertForbiddenOperationException, AlertConfigurationException {
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
    public void testUpdateUser() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        String another_role = "ANOTHER_ROLE";
        String admin_role = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN;
        Set<String> roleNames = new LinkedHashSet<>(Arrays.asList(admin_role, another_role));
        Set<UserRoleModel> roles = roleNames.stream().map(UserRoleModel::of).collect(Collectors.toSet());
        UserModel updatedModel = userAccessor.updateUser(UserModel.existingUser(userModel.getId(), userModel.getName(), userModel.getPassword(), userModel.getEmailAddress(), AuthenticationType.DATABASE, roles, true), true);
        assertEquals(userModel.getName(), updatedModel.getName());
        assertEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());
        assertEquals(userModel.getPassword(), updatedModel.getPassword());
        assertEquals(1, updatedModel.getRoles().size());

        assertFalse(updatedModel.hasRole(another_role));
        assertTrue(updatedModel.hasRole(admin_role));
        assertFalse(updatedModel.isExternal());
        userAccessor.deleteUser(userName);
    }

    @Test
    public void testChangeUserPassword() throws AlertForbiddenOperationException, AlertConfigurationException {
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
    public void testChangeUserEmailAddress() throws AlertForbiddenOperationException, AlertConfigurationException {
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

        UserModel updatedModel = foundModel.get();
        assertEquals(userModel.getName(), updatedModel.getName());
        assertNotEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());

        userAccessor.deleteUser(userName);

        assertFalse(userAccessor.changeUserEmailAddress("bad_user_name", "new_test_email"));
    }

    @Test
    public void testExternalUserUpdateRoles() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserRoleModel userRole = UserRoleModel.of(DefaultUserRole.ALERT_ADMIN.name());
        Set<UserRoleModel> roles = Set.of(userRole);
        UserModel existingUser = UserModel.existingUser(userModel.getId(), userName, null, email, AuthenticationType.LDAP, roles, true);
        UserModel updatedUser = userAccessor.updateUser(existingUser, true);

        assertEquals(roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet()), updatedUser.getRoles().stream().map(UserRoleModel::getName).collect(Collectors.toSet()));
        userAccessor.deleteUser(userName);
    }

    @Test
    public void testExternalUserUpdateNameException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";

        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(userModel.getId(), userName + "_updated", null, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        testUserUpdateException(updatedUser);
        userAccessor.deleteUser(userName);
    }

    @Test
    public void testExternalUserUpdateEmailException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(userModel.getId(), userName, null, email + "_updated", AuthenticationType.LDAP, Collections.emptySet(), true);
        testUserUpdateException(updatedUser);
        userAccessor.deleteUser(userName);
    }

    @Test
    public void testExternalUserUpdatePasswordException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(userModel.getId(), userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        testUserUpdateException(updatedUser);
        userAccessor.deleteUser(userName);
    }

    private void testUserUpdateException(UserModel updatedUser) {
        String exceptionMessage = "An external user cannot change its credentials.";
        try {
            userAccessor.updateUser(updatedUser, true);
            fail();
        } catch (AlertForbiddenOperationException | AlertConfigurationException ex) {
            assertTrue(ex.getMessage().contains(exceptionMessage));
        }
    }

}
