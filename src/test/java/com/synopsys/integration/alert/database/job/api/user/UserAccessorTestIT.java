package com.synopsys.integration.alert.database.job.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
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

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.enumeration.DefaultUserRole;
import com.blackduck.integration.alert.common.exception.AlertForbiddenOperationException;
import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.job.api.DefaultUserAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Transactional
@AlertIntegrationTest
class UserAccessorTestIT {
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
    void testGetUsers() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName1 = "testUser_1";
        String password1 = "testPassword_1";
        String email1 = "testEmail_1";
        userAccessor.addUser(userName1, password1, email1);

        String userName2 = "testUser_2";
        String password2 = "testPassword_2";
        String email2 = "testEmail_2";
        userAccessor.addUser(userName2, password2, email2);

        List<UserModel> modelList = userAccessor.getUsers();

        // default admin, jobmanager, alertuser user will be included
        assertEquals(5, modelList.size());
        userAccessor.deleteUser(userName1);
        userAccessor.deleteUser(userName2);
        modelList = userAccessor.getUsers();
        assertEquals(3, modelList.size());
    }

    @Test
    void testGetUserByUserName() {
        Optional<UserModel> user = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(user.isPresent());
    }

    @Test
    void testGetUserByUserNameNotFound() {
        Optional<UserModel> user = userAccessor.getUser("anUnknownUser");
        assertFalse(user.isPresent());
    }

    @Test
    void testAddUser() throws AlertForbiddenOperationException, AlertConfigurationException {
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
    void testUpdateUser() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = userAccessor.addUser(userName, password, email);

        assertNotNull(userModel);
        assertEquals(userName, userModel.getName());
        assertEquals(email, userModel.getEmailAddress());
        assertTrue(userModel.getRoles().isEmpty());

        String anotherRole = "ANOTHER_ROLE";
        String adminRole = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN;
        Set<String> roleNames = new LinkedHashSet<>(Arrays.asList(adminRole, anotherRole));
        Set<UserRoleModel> roles = roleNames.stream().map(UserRoleModel::of).collect(Collectors.toSet());
        UserModel updatedModel = userAccessor.updateUser(UserModel.existingUser(
            userModel.getId(),
            userModel.getName(),
            userModel.getPassword(),
            userModel.getEmailAddress(),
            AuthenticationType.DATABASE,
            roles,
            false,
            true,
            OffsetDateTime.now(),
            null,
            0L
        ), true);
        assertEquals(userModel.getName(), updatedModel.getName());
        assertEquals(userModel.getEmailAddress(), updatedModel.getEmailAddress());
        assertEquals(userModel.getPassword(), updatedModel.getPassword());
        assertEquals(1, updatedModel.getRoles().size());

        assertFalse(updatedModel.hasRole(anotherRole));
        assertTrue(updatedModel.hasRole(adminRole));
        assertFalse(updatedModel.isExternal());
        userAccessor.deleteUser(userName);
    }

    @Test
    void testChangeUserPassword() throws AlertForbiddenOperationException, AlertConfigurationException {
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
        UserModel updatedModel = foundModel.get();
        assertEquals(userModel.getName(), updatedModel.getName());
        assertNotEquals(userModel.getPassword(), updatedModel.getPassword());

        userAccessor.deleteUser(userName);

        assertFalse(userAccessor.changeUserPassword("bad_user_name", "new_test_password"));
    }

    @Test
    void testChangeUserEmailAddress() throws AlertForbiddenOperationException, AlertConfigurationException {
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
    void testExternalUserUpdateRoles() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserRoleModel userRole = UserRoleModel.of(DefaultUserRole.ALERT_ADMIN.name());
        Set<UserRoleModel> roles = Set.of(userRole);
        UserModel existingUser = UserModel.existingUser(userModel.getId(), userName, null, email, AuthenticationType.LDAP, roles, false, true, OffsetDateTime.now(), null, 0L);
        UserModel updatedUser = userAccessor.updateUser(existingUser, true);

        assertEquals(roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet()), updatedUser.getRoles().stream().map(UserRoleModel::getName).collect(Collectors.toSet()));
        userAccessor.deleteUser(userName);
    }

    @Test
    void testExternalUserUpdateNameException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";

        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(
            userModel.getId(),
            userName + "_updated",
            null,
            email,
            AuthenticationType.LDAP,
            Collections.emptySet(),
            false,
            true,
            OffsetDateTime.now(),
            null,
            0L
        );
        testUserUpdateException(updatedUser);
        userAccessor.deleteUser(userName);
    }

    @Test
    void testExternalUserUpdateEmailException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(
            userModel.getId(),
            userName,
            null,
            email + "_updated",
            AuthenticationType.LDAP,
            Collections.emptySet(),
            false,
            true,
            OffsetDateTime.now(),
            null,
            0L
        );
        testUserUpdateException(updatedUser);
        userAccessor.deleteUser(userName);
    }

    @Test
    void testExternalUserUpdatePasswordException() throws AlertForbiddenOperationException, AlertConfigurationException {
        String userName = "testUser";
        String password = "testPassword";
        String email = "testEmail";
        UserModel userModel = UserModel.newUser(userName, password, email, AuthenticationType.LDAP, Collections.emptySet(), true);
        userModel = userAccessor.addUser(userModel, false);
        UserModel updatedUser = UserModel.existingUser(
            userModel.getId(),
            userName,
            password,
            email,
            AuthenticationType.LDAP,
            Collections.emptySet(),
            false,
            true,
            OffsetDateTime.now(),
            null,
            0L
        );
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
