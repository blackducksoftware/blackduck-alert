/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;

@ExtendWith(SpringExtension.class)
class AzureBoardsGlobalValidationActionTest {
    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    @Mock
    private AzureBoardsGlobalConfigAccessor mockAzureBoardsGlobalConfigAccessor;

    private AzureBoardsGlobalConfigModel model;
    private AzureBoardsGlobalConfigurationValidator validator;

    @BeforeEach
    void initEach() {
        model = new AzureBoardsGlobalConfigModel();
        validator = new AzureBoardsGlobalConfigurationValidator(mockAzureBoardsGlobalConfigAccessor);
    }

    @Test
    void validateReturnsSuccessOnFullPermissions() {
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authManager = authenticationTestUtils
            .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

        AzureBoardsGlobalValidationAction validationAction = new AzureBoardsGlobalValidationAction(validator, authManager);

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    void validateReturnsForbiddenOnNoPermissions() {
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authManager = authenticationTestUtils
            .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

        AzureBoardsGlobalValidationAction validationAction = new AzureBoardsGlobalValidationAction(validator, authManager);

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }
}
