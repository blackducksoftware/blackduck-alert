/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.users.web.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DefaultUserRole;
import com.blackduck.integration.alert.common.exception.AlertForbiddenOperationException;
import com.blackduck.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.persistence.model.UserRoleModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.users.UserManagementDescriptorKey;
import com.blackduck.integration.alert.component.users.UserSystemValidator;
import com.blackduck.integration.alert.component.users.web.user.util.UserCredentialValidator;

class UserActionsTest {
    private final Long id = 10L;
    private final String name = "user";
    private final String password = "testPassword123!@#";
    private final String emailAddress = "noreply@blackduck.com";
    private final OffsetDateTime lastLogin = OffsetDateTime.now();
    private final long failedLoginCount = 0L;
    private final Set<UserRoleModel> roles = Set.of();
    private final AuthenticationType authenticationType = AuthenticationType.DATABASE;
    private final UserManagementDescriptorKey userManagementDescriptorKey = new UserManagementDescriptorKey();

    private UserAccessor userAccessor;
    private RoleAccessor roleAccessor;
    private AuthorizationManager authorizationManager;
    private AuthenticationTypeAccessor authenticationTypeAccessor;
    private UserSystemValidator userSystemValidator;
    private UserCredentialValidator userCredentialValidator;

    @BeforeEach
    public void init() {
        userAccessor = Mockito.mock(UserAccessor.class);
        roleAccessor = Mockito.mock(RoleAccessor.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);
        userSystemValidator = Mockito.mock(UserSystemValidator.class);
        userCredentialValidator = new UserCredentialValidator();

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
    }

    @Test
    void testFindExisting() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        Optional<UserConfig> userConfigOptional = userActions.findExisting(id);

        assertTrue(userConfigOptional.isPresent());
        UserConfig userConfig = userConfigOptional.get();
        assertUserConfig(userConfig);
    }

    @Test
    void testFindExistingEmpty() {
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        Optional<UserConfig> userConfigOptional = userActions.findExisting(id);

        assertFalse(userConfigOptional.isPresent());
    }

    @Test
    void testReadAllWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);
        AuthenticationTypeDetails authenticationTypeDetails = new AuthenticationTypeDetails(1L, authenticationType.name());

        Mockito.when(userAccessor.getUsers()).thenReturn(List.of(userModel));
        Mockito.when(authenticationTypeAccessor.getAuthenticationTypeDetails(Mockito.any())).thenReturn(Optional.of(authenticationTypeDetails));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<MultiUserConfigResponseModel> actionResponse = userActions.getAll();

        assertTrue(actionResponse.hasContent());
        List<UserConfig> userModels = actionResponse.getContent()
            .map(MultiUserConfigResponseModel::getUsers)
            .orElse(List.of());
        assertEquals(1, userModels.size());
        UserConfig userConfig = userModels.get(0);
        assertUserConfig(userConfig);
        assertEquals(authenticationType.name(), userConfig.getAuthenticationType());
        assertNull(userConfig.getPassword());
    }

    @Test
    void testReadWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.getUser(2L)).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> actionResponse = userActions.getOne(id);
        ActionResponse<UserConfig> actionResponseEmpty = userActions.getOne(2L);

        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.getContent().isPresent());
        UserConfig userConfig = actionResponse.getContent().get();
        assertUserConfig(userConfig);

        assertFalse(actionResponseEmpty.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseEmpty.getHttpStatus());
    }

    @Test
    void testTestWithoutChecks() {
        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        roleNames.add(DefaultUserRole.ALERT_ADMIN.name());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ValidationActionResponse validationActionResponse = userActions.testWithoutChecks(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        assertTrue(validationActionResponse.getContent().isPresent());
        assertFalse(validationActionResponse.getContent().get().hasErrors());
    }

    @Test
    void testCreateWithoutChecks() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.addUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(userModel);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.createWithoutChecks(userConfig);

        assertFalse(userConfigActionResponse.isError());
        assertTrue(userConfigActionResponse.hasContent());
        assertTrue(userConfigActionResponse.getContent().isPresent());
        UserConfig testUserConfig = userConfigActionResponse.getContent().get();
        assertUserConfig(testUserConfig);
        assertEquals(HttpStatus.CREATED, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testCreateWithoutChecksDatabaseError() throws Exception {
        Mockito.when(userAccessor.addUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new AlertConfigurationException("Exception for test"));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.createWithoutChecks(userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testUpdateWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertFalse(userConfigActionResponse.isError());
        assertEquals(HttpStatus.NO_CONTENT, userConfigActionResponse.getHttpStatus());
        assertFalse(userConfigActionResponse.hasContent());
    }

    @Test
    void testUpdateWithoutChecksNoRoles() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));

        UserConfig userConfig = new UserConfig(id.toString(), name, password, "newEmailAddress", Set.of(), false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertFalse(userConfigActionResponse.isError());
        assertEquals(HttpStatus.NO_CONTENT, userConfigActionResponse.getHttpStatus());
        assertFalse(userConfigActionResponse.hasContent());

        ActionResponse<UserConfig> returnConfigActionResponse = userActions.getOne(id);
        assertTrue(returnConfigActionResponse.getContent().isPresent());
        UserConfig returnUserConfig = returnConfigActionResponse.getContent().get();
        assertTrue(returnUserConfig.getRoleNames().isEmpty());
    }

    @Test
    void testUpdateWithoutChecksDatabaseError() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.updateUser(Mockito.any(), Mockito.anyBoolean())).thenThrow(new AlertConfigurationException("Exception for test"));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testUpdateWithoutChecksNotFound() {
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.empty());

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertEquals(HttpStatus.NOT_FOUND, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testDeleteWithoutChecks() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        Mockito.verify(userAccessor).deleteUser(id);
        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testDeleteWithoutChecksException() throws AlertForbiddenOperationException {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.doThrow(new AlertForbiddenOperationException("Exception for test")).when(userAccessor).deleteUser(id);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testDeleteWithoutChecksEmpty() {
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, userConfigActionResponse.getHttpStatus());
    }

    @Test
    void testInternalUserNoEmailValidation() {
        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, null, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);

        ValidationActionResponse validationActionResponse = userActions.validate(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        assertTrue(validationActionResponse.getContent().isPresent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getErrors().containsKey(UserActions.FIELD_KEY_USER_MGMT_EMAILADDRESS));
    }

    @Test
    void testExternalUserNoEmailValidation() {
        AuthenticationType authenticationTypeLDAP = AuthenticationType.LDAP;

        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationTypeLDAP, roles, false, true, lastLogin, null, failedLoginCount);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        roleNames.add(DefaultUserRole.ALERT_ADMIN.name());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, null, roleNames, false, false, false, true, false, authenticationTypeLDAP.name(), true);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);

        ValidationActionResponse validationActionResponse = userActions.validate(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        assertTrue(validationActionResponse.getContent().isPresent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
        assertTrue(validationActionResponse.getMessage().isPresent());
        assertEquals("The user is valid", validationActionResponse.getMessage().get());
    }

    @Test
    void invalidPasswordValidateWithoutChecksTest() {
        //Test with a password that does not contain a digit or special character, resulting in a validation error.
        String invalidPassword = "invalidPassword";
        UserModel userModel = UserModel.existingUser(id, name, invalidPassword, null, AuthenticationType.DATABASE, Set.of(), false, true, lastLogin, null, failedLoginCount);
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        UserConfig userConfig = new UserConfig(id.toString(), name, invalidPassword, null, Set.of(), false, false, false, true, false, AuthenticationType.DATABASE.name(), true);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ValidationActionResponse actionResponse = userActions.validateWithoutChecks(userConfig);

        assertTrue(actionResponse.isError());
        ValidationResponseModel validationResponseModel = actionResponse.getContent().orElseThrow(() -> new AssertionError("Expected content but none was found"));
        assertTrue(validationResponseModel.hasErrors());
        Map<String, AlertFieldStatus> errors = validationResponseModel.getErrors();
        assertTrue(errors.containsKey(UserActions.FIELD_KEY_USER_MGMT_PASSWORD));
        String fieldErrorMessage = errors.get(UserActions.FIELD_KEY_USER_MGMT_PASSWORD).getFieldMessage();
        assertTrue(fieldErrorMessage.contains(UserCredentialValidator.PASSWORD_NO_DIGIT_MESSAGE));
        assertTrue(fieldErrorMessage.contains(UserCredentialValidator.PASSWORD_NO_SPECIAL_CHARACTER_MESSAGE));
    }

    @Test
    void invalidPasswordCreateTest() {
        //Test with a password that does not contain a digit or special character, resulting in a validation error.
        String invalidPassword = "invalidPassword";
        UserModel userModel = UserModel.existingUser(id, name, invalidPassword, null, AuthenticationType.DATABASE, Set.of(), false, true, lastLogin, null, failedLoginCount);
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        UserConfig userConfig = new UserConfig(id.toString(), name, invalidPassword, null, Set.of(), false, false, false, true, false, AuthenticationType.DATABASE.name(), true);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> actionResponse = userActions.create(userConfig);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void invalidPasswordUpdateTest() {
        //Test with a password that does not contain a digit or special character, resulting in a validation error.
        String invalidPassword = "invalidPassword";
        UserModel userModel = UserModel.existingUser(id, name, invalidPassword, null, AuthenticationType.DATABASE, Set.of(), false, true, lastLogin, null, failedLoginCount);
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        UserConfig userConfig = new UserConfig(id.toString(), name, invalidPassword, null, Set.of(), false, false, false, true, false, AuthenticationType.DATABASE.name(), true);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ActionResponse<UserConfig> actionResponse = userActions.update(id, userConfig);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void validValidateWithoutChecksTest() {
        UserModel userModel = UserModel.existingUser(id, name, password, null, AuthenticationType.DATABASE, Set.of(), false, true, lastLogin, null, failedLoginCount);
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        UserConfig userConfig = new UserConfig(id.toString(), name, password, null, Set.of(), false, false, false, true, false, AuthenticationType.DATABASE.name(), true);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator, userCredentialValidator);
        ValidationActionResponse actionResponse = userActions.validateWithoutChecks(userConfig);
        assertTrue(actionResponse.isSuccessful());
    }

    private void assertUserConfig(UserConfig userConfig) {
        assertEquals(name, userConfig.getUsername());
        assertEquals(emailAddress, userConfig.getEmailAddress());
    }

}
