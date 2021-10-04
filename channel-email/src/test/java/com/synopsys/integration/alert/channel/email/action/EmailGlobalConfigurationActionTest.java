package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.channel.email.web.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

public class EmailGlobalConfigurationActionTest {
    @Test
    public void testGetOne() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");
        DatabaseModelWrapper<EmailGlobalConfigModel> modelWrapper = new DatabaseModelWrapper<>(1L, 1L, "now", "then", model);
        Mockito.when(emailGlobalConfigAccessor.getConfiguration(Mockito.anyLong())).thenReturn(Optional.of(modelWrapper));

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.getOne(1L);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertEquals(model, response.getContent().get());
    }

    @Test
    public void testCreate() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");
        DatabaseModelWrapper<EmailGlobalConfigModel> modelWrapper = new DatabaseModelWrapper<>(1L, 1L, "now", "then", model);
        Mockito.when(emailGlobalConfigAccessor.createConfiguration(Mockito.eq(model))).thenReturn(modelWrapper);

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.create(model);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertEquals(model, response.getContent().get());
    }

    @Test
    public void testUpdate() throws AlertConfigurationException {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");
        DatabaseModelWrapper<EmailGlobalConfigModel> modelWrapper = new DatabaseModelWrapper<>(1L, 1L, "now", "then", model);
        Mockito.when(emailGlobalConfigAccessor.getConfiguration(Mockito.anyLong())).thenReturn(Optional.of(modelWrapper));
        Mockito.when(emailGlobalConfigAccessor.updateConfiguration(Mockito.anyLong(), Mockito.eq(model))).thenReturn(modelWrapper);

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.update(1L, model);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
        assertEquals(model, response.getContent().get());
    }

    @Test
    public void testDelete() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");
        DatabaseModelWrapper<EmailGlobalConfigModel> modelWrapper = new DatabaseModelWrapper<>(1L, 1L, "now", "then", model);
        Mockito.when(emailGlobalConfigAccessor.getConfiguration(Mockito.anyLong())).thenReturn(Optional.of(modelWrapper));

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.delete(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    }

    @Test
    public void testGetOneForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.getOne(1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testCreateForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.create(model);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testUpdateForbidden() throws AlertConfigurationException {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.update(1L, model);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testUpdateNotFound() throws AlertConfigurationException {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(emailGlobalConfigAccessor.getConfiguration(Mockito.anyLong())).thenReturn(Optional.empty());
        
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.update(1L, model);
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

    @Test
    public void testDeleteForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.delete(1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testDeleteNotFound() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(emailGlobalConfigAccessor.getConfiguration(Mockito.anyLong())).thenReturn(Optional.empty());

        EmailGlobalConfigActions configActions = new EmailGlobalConfigActions(authorizationManager, emailGlobalConfigAccessor, validator);
        ActionResponse<EmailGlobalConfigModel> response = configActions.delete(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

}
