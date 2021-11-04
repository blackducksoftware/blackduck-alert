package com.synopsys.integration.alert.common.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.function.ThrowingSupplier;

public class ConfigurationCrudHelperTest {
    @Test
    public void testGetOneForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 0);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.getOne(() -> createOptionalDefault());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testGetOneEmpty() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.getOne(() -> createEmptyOptional());
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

    @Test
    public void testGetOneHasContent() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.getOne(() -> createOptionalDefault());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.hasContent());
    }

    @Test
    public void testCreateForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.create(() -> ValidationResponseModel.success(), () -> createDefault());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testCreateValidationError() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.create(() -> ValidationResponseModel.generalError("Validation Error"), () -> createDefault());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void testCreateException() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ThrowingSupplier<TestObfuscatedVariable, Exception> modelCreator = () -> {
            throw new AlertException("error getting test message");
        };
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.create(() -> ValidationResponseModel.success(), modelCreator);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void testCreateSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.create(() -> ValidationResponseModel.success(), () -> createDefault());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    public void testUpdateForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.update(() -> ValidationResponseModel.success(), () -> true, () -> createDefault());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testUpdateModelNotFound() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.update(() -> ValidationResponseModel.success(), () -> false, () -> createDefault());
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

    @Test
    public void testUpdateValidationError() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.update(() -> ValidationResponseModel.generalError("Validation Error"), () -> true, () -> createDefault());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void testUpdateException() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ThrowingSupplier createdModelSupplier = () -> {
            throw new AlertException("error getting test message");
        };
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.update(() -> ValidationResponseModel.success(), () -> true, createdModelSupplier);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void testUpdateSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.update(() -> ValidationResponseModel.success(), () -> true, () -> createDefault());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    public void testDeleteForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ActionResponse response = configurationHelper.delete(() -> true, () -> {});
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testDeleteModelNotFound() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.delete(() -> false, () -> {});
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

    @Test
    public void testDeleteSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationCrudHelper configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);

        ActionResponse response = configurationHelper.delete(() -> true, () -> {});
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    }

    private Optional<TestObfuscatedVariable> createOptionalDefault() {
        return Optional.of(new TestObfuscatedVariable("Model String"));
    }

    private Optional<TestObfuscatedVariable> createEmptyOptional() {
        return Optional.empty();
    }

    private TestObfuscatedVariable createDefault() {
        return new TestObfuscatedVariable("Model String");
    }

    class TestObfuscatedVariable implements Obfuscated<TestObfuscatedVariable> {
        private String variable;

        TestObfuscatedVariable(final String variable) {
            this.variable = variable;
        }

        public String getVariable() {
            return variable;
        }

        public void setVariable(final String variable) {
            this.variable = variable;
        }

        @Override
        public TestObfuscatedVariable obfuscate() {
            return this;
        }
    }
}
