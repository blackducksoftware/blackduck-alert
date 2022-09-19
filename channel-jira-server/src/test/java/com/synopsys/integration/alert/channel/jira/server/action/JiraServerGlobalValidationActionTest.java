package com.synopsys.integration.alert.channel.jira.server.action;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class JiraServerGlobalValidationActionTest {
    AuthenticationTestUtils authenticationTestUtils;
    DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
    PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());

    @Mock
    JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    JiraServerGlobalConfigModel model;
    JiraServerGlobalConfigurationValidator validator;

    @BeforeEach
    void init() {
        authenticationTestUtils = new AuthenticationTestUtils();
        permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());

        model = new JiraServerGlobalConfigModel();
        validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
    }

    @Test
    void validateReturnsSuccessOnFullPermissions() {
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authManager = authenticationTestUtils
                .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

        JiraServerGlobalValidationAction validationAction = new JiraServerGlobalValidationAction(validator, authManager);

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    void validateReturnsForbiddenOnNoPermissions() throws AlertConfigurationException {
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authManager = authenticationTestUtils
                .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

        JiraServerGlobalValidationAction validationAction = new JiraServerGlobalValidationAction(validator, authManager);

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }
}
