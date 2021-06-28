package com.synopsys.integration.alert.component.users.web.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.component.users.UserSystemValidator;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class UserActionsTest {
    private final Long id = 1L;
    private final String name = "user";
    private final String password = "password";
    private final String emailAddress = "noreply@synopsys.com";
    private final Set<UserRoleModel> roles = Set.of();
    private final AuthenticationType authenticationType = AuthenticationType.DATABASE;

    private UserManagementDescriptorKey userManagementDescriptorKey;
    private UserAccessor userAccessor;
    private RoleAccessor roleAccessor;
    private AuthorizationManager authorizationManager;
    private AuthenticationTypeAccessor authenticationTypeAccessor;
    private UserSystemValidator userSystemValidator;

    @BeforeEach
    public void init() {
        userManagementDescriptorKey = Mockito.mock(UserManagementDescriptorKey.class);
        userAccessor = Mockito.mock(UserAccessor.class);
        roleAccessor = Mockito.mock(RoleAccessor.class);
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        authenticationTypeAccessor = Mockito.mock(AuthenticationTypeAccessor.class);
        userSystemValidator = Mockito.mock(UserSystemValidator.class);
    }

    @Test
    public void testFindExisting() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        Optional<UserConfig> userConfigOptional = userActions.findExisting(id);

        assertTrue(userConfigOptional.isPresent());
        UserConfig userConfig = userConfigOptional.get();
        assertUserConfig(userConfig);
    }

    @Test
    public void testFindExistingEmpty() {
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        Optional<UserConfig> userConfigOptional = userActions.findExisting(id);

        assertFalse(userConfigOptional.isPresent());
    }

    @Test
    public void testReadAllWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);
        AuthenticationTypeDetails authenticationTypeDetails = new AuthenticationTypeDetails(1L, authenticationType.name());

        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(userAccessor.getUsers()).thenReturn(List.of(userModel));
        Mockito.when(authenticationTypeAccessor.getAuthenticationTypeDetails(Mockito.any())).thenReturn(Optional.of(authenticationTypeDetails));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<MultiUserConfigResponseModel> actionResponse = userActions.getAll();

        assertTrue(actionResponse.hasContent());
        List<UserConfig> userModels = actionResponse.getContent()
                                          .get()
                                          .getUsers();
        assertEquals(1, userModels.size());
        UserConfig userConfig = userModels.get(0);
        assertUserConfig(userConfig);
        assertEquals(authenticationType.name(), userConfig.getAuthenticationType());
        assertNull(userConfig.getPassword());
    }

    @Test
    public void testReadWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.getUser(2L)).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> actionResponse = userActions.getOne(id);
        ActionResponse<UserConfig> actionResponseEmpty = userActions.getOne(2L);

        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        UserConfig userConfig = actionResponse.getContent().get();
        assertUserConfig(userConfig);

        assertFalse(actionResponseEmpty.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseEmpty.getHttpStatus());
    }

    @Test
    public void testTestWithoutChecks() {
        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        roleNames.add(DefaultUserRole.ALERT_ADMIN.name());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ValidationActionResponse validationActionResponse = userActions.testWithoutChecks(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        assertFalse(validationActionResponse.getContent().get().hasErrors());
    }

    @Test
    public void testCreateWithoutChecks() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.addUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(userModel);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.createWithoutChecks(userConfig);

        assertFalse(userConfigActionResponse.isError());
        assertTrue(userConfigActionResponse.hasContent());
        UserConfig testUserConfig = userConfigActionResponse.getContent().get();
        assertUserConfig(testUserConfig);
        assertEquals(HttpStatus.CREATED, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testCreateWithoutChecksDatabaseError() throws Exception {
        Mockito.when(userAccessor.addUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new AlertConfigurationException("Exception for test"));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.createWithoutChecks(userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testUpdateWithoutChecks() {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertFalse(userConfigActionResponse.isError());
        assertEquals(HttpStatus.NO_CONTENT, userConfigActionResponse.getHttpStatus());
        assertFalse(userConfigActionResponse.hasContent());
    }

    @Test
    public void testUpdateWithoutChecksDatabaseError() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.updateUser(Mockito.any(), Mockito.anyBoolean())).thenThrow(new AlertConfigurationException("Exception for test"));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testUpdateWithoutChecksNotFound() {
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.empty());

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", "newEmailAddress", roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.updateWithoutChecks(id, userConfig);

        assertTrue(userConfigActionResponse.isError());
        assertEquals(HttpStatus.NOT_FOUND, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testDeleteWithoutChecks() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        Mockito.verify(userAccessor).deleteUser(id);
        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testDeleteWithoutChecksException() throws AlertForbiddenOperationException {
        UserModel userModel = UserModel.existingUser(id, name, password, emailAddress, authenticationType, roles, true);

        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.of(userModel));
        Mockito.doThrow(new AlertForbiddenOperationException("Exception for test")).when(userAccessor).deleteUser(id);

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testDeleteWithoutChecksEmpty() {
        Mockito.when(userAccessor.getUser(id)).thenReturn(Optional.empty());

        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);
        ActionResponse<UserConfig> userConfigActionResponse = userActions.deleteWithoutChecks(id);

        assertFalse(userConfigActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, userConfigActionResponse.getHttpStatus());
    }

    @Test
    public void testInternalUserNoEmailValidation() throws Exception {
        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationType, roles, true);

        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationType.name(), false);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);

        ValidationActionResponse validationActionResponse = userActions.validate(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getErrors().containsKey(UserActions.FIELD_KEY_USER_MGMT_EMAILADDRESS));
    }

    @Test
    public void testExternalUserNoEmailValidation() throws Exception {
        AuthenticationType authenticationTypeLDAP = AuthenticationType.LDAP;

        UserModel userModel = UserModel.existingUser(id, name, password, null, authenticationTypeLDAP, roles, true);

        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(userAccessor.getUser(Mockito.anyLong())).thenReturn(Optional.of(userModel));

        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        roleNames.add(DefaultUserRole.ALERT_ADMIN.name());
        UserConfig userConfig = new UserConfig(id.toString(), name, "newPassword", null, roleNames, false, false, false, true, false, authenticationTypeLDAP.name(), true);
        UserActions userActions = new UserActions(userManagementDescriptorKey, userAccessor, roleAccessor, authorizationManager, authenticationTypeAccessor, userSystemValidator);

        ValidationActionResponse validationActionResponse = userActions.validate(userConfig);

        assertFalse(validationActionResponse.isError());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
        assertEquals("The user is valid", validationActionResponse.getMessage().get());
    }

    private void assertUserConfig(UserConfig userConfig) {
        assertEquals(name, userConfig.getUsername());
        assertEquals(emailAddress, userConfig.getEmailAddress());
    }

}
