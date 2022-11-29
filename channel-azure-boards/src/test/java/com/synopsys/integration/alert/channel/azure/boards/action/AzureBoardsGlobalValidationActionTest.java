package com.synopsys.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

@ExtendWith(SpringExtension.class)
class AzureBoardsGlobalValidationActionTest {
    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
    @Mock
    private AzureBoardsGlobalConfigAccessor mockAzureBoardsGlobalConfigAccessor;

    private AzureBoardsGlobalConfigModel model;
    private AzureBoardsGlobalConfigurationValidator validator;

    @BeforeEach
    void initEach() {
        model = new AzureBoardsGlobalConfigModel();
        validator = new AzureBoardsGlobalConfigurationValidator(mockAzureBoardsGlobalConfigAccessor, oAuthRequestValidator);
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
