package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

public class EmailGlobalValidationActionTest {
    @Test
    public void testValidationSuccess() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalValidationAction validationAction = new EmailGlobalValidationAction(validator, authorizationManager);

        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    public void testValidationForbidden() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.EMAIL;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.NO_PERMISSIONS);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalValidationAction validationAction = new EmailGlobalValidationAction(validator, authorizationManager);

        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ActionResponse<ValidationResponseModel> response = validationAction.validate(model);
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }
}
