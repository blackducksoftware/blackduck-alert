package com.blackduck.integration.alert.common.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;

public class TestHelperConfigurationTest {
    @Test
    public void testForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ValidationActionResponse response = testHelper.test(() -> new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success()), () -> ConfigurationTestResult.success("Success"));
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void testValidationSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ValidationActionResponse response = testHelper.test(() -> new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success()), () -> ConfigurationTestResult.success("Success"));
        assertEquals(HttpStatus.OK, response.getHttpStatus());

    }

    @Test
    public void testValidationWithError() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ValidationActionResponse response = testHelper.test(() -> new ValidationActionResponse(HttpStatus.BAD_REQUEST, ValidationResponseModel.generalError("generalError")), () -> ConfigurationTestResult.success("Success"));
        ValidationResponseModel validationResponseModel = response.getContent().orElseThrow(() -> new IllegalStateException("Validation content missing"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testMessageSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ValidationActionResponse response = testHelper.test(() -> new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success()), () -> ConfigurationTestResult.success("Success"));
        ValidationResponseModel validationResponseModel = response.getContent().orElseThrow(() -> new IllegalStateException("Validation content missing"));
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void testMessageWithErrors() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = new ChannelKey("channel_key", "channel-display-name");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        ConfigurationTestHelper testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, descriptorKey);
        ValidationActionResponse response = testHelper.test(() -> new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success()), () -> ConfigurationTestResult.failure("Failure"));
        ValidationResponseModel validationResponseModel = response.getContent().orElseThrow(() -> new IllegalStateException("Validation content missing"));
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(validationResponseModel.hasErrors());
    }
}
