package com.synopsys.integration.alert.database.job.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

class DefaultUserAccessorTest {
    private final String username = "username";
    private final String password = "password";
    private final String emailAddress = "noreply@blackducksoftware.com";
    private final OffsetDateTime lastLogin = OffsetDateTime.now();
    private final Long failedLoginCount = 0L;

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private PasswordEncoder defaultPasswordEncoder;
    private DefaultRoleAccessor roleAccessor;
    private AuthenticationTypeAccessor authenticationTypeAccessor;

    @BeforeEach
    public void init() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.userRoleRepository = Mockito.mock(UserRoleRepository.class);
        this.defaultPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        this.roleAccessor = Mockito.mock(DefaultRoleAccessor.class);
        this.authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);
    }

    @Test
    void getUsersTest() {
        final Long authenticationTypeId = 1L;
        final String roleName = "userName";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, authenticationTypeId, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findAll()).thenReturn(List.of(userEntity));
        createModelMocks(userRoleRelation, userRoleModel, AuthenticationType.DATABASE);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        List<UserModel> userModelList = defaultUserAccessor.getUsers();

        assertEquals(1, userModelList.size());
        UserModel userModel = userModelList.get(0);
        testUserModel(userEntity.getId(), roleName, userModel);
    }

    @Test
    void getUserByUserIdTest() {
        final Long userId = 1L;
        final Long emptyUserId = 5L;
        final Long authenticationTypeId = 1L;
        final String roleName = "userName";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, authenticationTypeId, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(emptyUserId)).thenReturn(Optional.empty());
        createModelMocks(userRoleRelation, userRoleModel, AuthenticationType.DATABASE);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        Optional<UserModel> userModelOptional = defaultUserAccessor.getUser(userId);
        Optional<UserModel> userModelOptionalEmpty = defaultUserAccessor.getUser(emptyUserId);

        assertTrue(userModelOptional.isPresent());
        assertFalse(userModelOptionalEmpty.isPresent());
        UserModel userModel = userModelOptional.get();
        testUserModel(userEntity.getId(), roleName, userModel);
    }

    @Test
    void getUserByUsernameTest() {
        final String emptyUsername = "";
        final Long authenticationTypeId = 1L;
        final String roleName = "userName";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, authenticationTypeId, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByUserName(emptyUsername)).thenReturn(Optional.empty());
        createModelMocks(userRoleRelation, userRoleModel, AuthenticationType.DATABASE);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        Optional<UserModel> userModelOptional = defaultUserAccessor.getUser(username);
        Optional<UserModel> userModelOptionalEmpty = defaultUserAccessor.getUser(emptyUsername);

        assertTrue(userModelOptional.isPresent());
        assertFalse(userModelOptionalEmpty.isPresent());
        UserModel userModel = userModelOptional.get();
        testUserModel(userEntity.getId(), roleName, userModel);
    }

    @Test
    void addUserTest() throws Exception {
        final String roleName = "userName";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        AuthenticationTypeDetails authenticationTypeDetails = new AuthenticationTypeDetails(2L, "authentication-name");
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findByUserName(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(authenticationTypeAccessor.getAuthenticationTypeDetails(Mockito.any())).thenReturn(Optional.of(authenticationTypeDetails));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(userEntity);
        createModelMocks(userRoleRelation, userRoleModel, AuthenticationType.DATABASE);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        UserModel userModel = defaultUserAccessor.addUser(username, password, emailAddress);

        testUserModel(userEntity.getId(), roleName, userModel);
    }

    @Test
    void addUserExistsTest() {
        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);

        Mockito.when(userRepository.findByUserName(Mockito.any())).thenReturn(Optional.of(userEntity));

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        try {
            defaultUserAccessor.addUser(username, password, emailAddress);
            fail("User with the same name that already exists in the userRepository did not throw expected " + AlertConfigurationException.class.getSimpleName());
        } catch (AlertConfigurationException e) {
            assertNotNull(e);
        }
    }

    @Test
    void updateUserTest() throws Exception {
        final String roleName = "userName";
        AuthenticationType authenticationType = AuthenticationType.DATABASE;

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserRoleModel roles = createUserRoleModel(roleName);
        UserModel userModel = UserModel.existingUser(1L, username, password, emailAddress, authenticationType, Set.of(roles), true, lastLogin, null, failedLoginCount);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(userEntity));
        Mockito.when(authenticationTypeAccessor.getAuthenticationType(Mockito.any())).thenReturn(Optional.of(authenticationType));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(userEntity);
        createModelMocks(userRoleRelation, userRoleModel, authenticationType);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        UserModel newUserModel = defaultUserAccessor.updateUser(userModel, false);

        Mockito.verify(roleAccessor).updateUserRoles(Mockito.eq(userEntity.getId()), Mockito.any());

        testUserModel(userEntity.getId(), roleName, newUserModel);
    }

    @Test
    void updateUserNonDatabaseAuthTest() throws Exception {
        final String roleName = "roleName";
        AuthenticationType authenticationType = AuthenticationType.LDAP;

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserRoleModel roles = createUserRoleModel(roleName);
        UserModel userModel = UserModel.existingUser(1L, username, "", emailAddress, authenticationType, Set.of(roles), true, lastLogin, null, failedLoginCount);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(userEntity));
        Mockito.when(authenticationTypeAccessor.getAuthenticationType(Mockito.any())).thenReturn(Optional.of(authenticationType));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(userEntity);
        createModelMocks(userRoleRelation, userRoleModel, authenticationType);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        UserModel updatedUserModel = defaultUserAccessor.updateUser(userModel, false);

        Mockito.verify(roleAccessor).updateUserRoles(Mockito.eq(userEntity.getId()), Mockito.any());

        testUserModel(userEntity.getId(), roleName, updatedUserModel);
    }

    @Test
    void updateUserNonDatabaseAuthInvalidTest() {
        final String roleName = "roleName";
        AuthenticationType authenticationType = AuthenticationType.LDAP;

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserEntity existingUserEntity = new UserEntity("usernam-teste", "existing-password", "old-email.noreply@blackducksoftware.com", 2L, lastLogin, null, failedLoginCount);
        existingUserEntity.setId(1L);
        UserRoleModel roles = createUserRoleModel(roleName);
        UserModel userModel = UserModel.existingUser(1L, username, password, emailAddress, authenticationType, Set.of(roles), true, lastLogin, null, failedLoginCount);
        UserRoleRelation userRoleRelation = new UserRoleRelation(1L, 2L);
        UserRoleModel userRoleModel = createUserRoleModel(roleName);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(existingUserEntity));
        Mockito.when(authenticationTypeAccessor.getAuthenticationType(Mockito.any())).thenReturn(Optional.of(authenticationType));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(existingUserEntity);
        createModelMocks(userRoleRelation, userRoleModel, authenticationType);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        try {
            defaultUserAccessor.updateUser(userModel, false);
            fail("External user with ? did not throw expected " + AlertForbiddenOperationException.class.getSimpleName());
        } catch (AlertForbiddenOperationException e) {
            assertNotNull(e);
        } catch (AlertConfigurationException wrongException) {
            fail("Wrong exception thrown");
        }
    }

    @Test
    void assignRolesTest() {
        final String badUsername = "badUsername";
        final Long roleId = 5L;

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByUserName(badUsername)).thenReturn(Optional.empty());

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        boolean assignedRoles = defaultUserAccessor.assignRoles(username, Set.of(roleId));
        boolean assignedRolesFalse = defaultUserAccessor.assignRoles(badUsername, Set.of(roleId));

        Mockito.verify(roleAccessor).updateUserRoles(Mockito.eq(userEntity.getId()), Mockito.any());
        Mockito.verify(roleAccessor).getRoles(Mockito.any());

        assertTrue(assignedRoles);
        assertFalse(assignedRolesFalse);
    }

    @Test
    void changeUserPasswordTest() {
        final String badUsername = "badUsername";
        final String newPassword = "newPassword";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserEntity newUserEntity = new UserEntity(username, newPassword, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByUserName(badUsername)).thenReturn(Optional.empty());
        Mockito.when(defaultPasswordEncoder.encode(Mockito.any())).thenReturn(newPassword);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(newUserEntity);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        assertTrue(defaultUserAccessor.changeUserPassword(username, newPassword));
        assertFalse(defaultUserAccessor.changeUserPassword(badUsername, newPassword));
    }

    @Test
    void changeUserEmailAddressTest() {
        final String badUsername = "badUsername";
        final String newEmailAddress = "newemail.noreplay@blackducksoftware.com";

        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);
        UserEntity newUserEntity = new UserEntity(username, password, newEmailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByUserName(badUsername)).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(newUserEntity);

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        assertTrue(defaultUserAccessor.changeUserEmailAddress(username, newEmailAddress));
        assertFalse(defaultUserAccessor.changeUserEmailAddress(badUsername, newEmailAddress));
    }

    @Test
    void deleteUserByNameTest() throws Exception {
        //userEntity Id's between 0 and 2 are reserved
        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(5L);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        defaultUserAccessor.deleteUser(username);

        Mockito.verify(roleAccessor).updateUserRoles(Mockito.any(), Mockito.any());
        Mockito.verify(userRepository).deleteById(Mockito.any());
    }

    @Test
    void deleteUserByIdTest() throws Exception {
        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(5L);

        Mockito.when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);
        defaultUserAccessor.deleteUser(userEntity.getId());

        Mockito.verify(roleAccessor).updateUserRoles(Mockito.any(), Mockito.any());
        Mockito.verify(userRepository).deleteById(Mockito.any());
    }

    @Test
    void deleteUserReservedIdTest() {
        UserEntity userEntity = new UserEntity(username, password, emailAddress, 2L, lastLogin, null, failedLoginCount);
        userEntity.setId(1L);

        Mockito.when(userRepository.findByUserName(username)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(userEntity));

        DefaultUserAccessor defaultUserAccessor = new DefaultUserAccessor(userRepository, userRoleRepository, defaultPasswordEncoder, roleAccessor, authenticationTypeAccessor);

        try {
            defaultUserAccessor.deleteUser(username);
            fail("A forbidden userEntity id did not throw the expected AlertForbiddenOperationException");
        } catch (AlertForbiddenOperationException e) {
            assertNotNull(e);
        }
    }

    private void createModelMocks(UserRoleRelation userRoleRelation, UserRoleModel userRoleModel, AuthenticationType authenticationType) {
        Mockito.when(userRoleRepository.findAllByUserId(Mockito.any())).thenReturn(List.of(userRoleRelation));
        Mockito.when(roleAccessor.getRoles(Mockito.any())).thenReturn(Set.of(userRoleModel));
        Mockito.when(authenticationTypeAccessor.getAuthenticationType(2L)).thenReturn(Optional.of(authenticationType));
    }

    private UserRoleModel createUserRoleModel(String name) {
        PermissionMatrixModel permissionMatrixModel = new PermissionMatrixModel(Map.of());
        return new UserRoleModel(1L, name, true, permissionMatrixModel);
    }

    private void testUserModel(Long expectedId, String expectedRoleName, UserModel userModel) {
        Assertions.assertEquals(expectedId, userModel.getId());
        Assertions.assertEquals(username, userModel.getName());
        Assertions.assertEquals(emailAddress, userModel.getEmailAddress());
        Assertions.assertTrue(userModel.getRoleNames().contains(expectedRoleName));
        Assertions.assertEquals(lastLogin, userModel.getLastLogin());
        Assertions.assertNull(userModel.getLastFailedLogin());
        Assertions.assertEquals(failedLoginCount, userModel.getFailedLoginCount());
    }

}
