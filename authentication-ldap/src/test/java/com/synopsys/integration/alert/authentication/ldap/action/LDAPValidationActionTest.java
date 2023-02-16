package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

class LDAPValidationActionTest {
    private static LDAPValidationAction ldapValidationAction;

    @BeforeEach
    public void init() {
        LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();

        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );

        ldapValidationAction = new LDAPValidationAction(ldapConfigurationValidator, authorizationManager, authenticationDescriptorKey);
    }

    @Test
    void testValidateInvalidConfigModel() {
        LDAPConfigModel emptyLDAPConfigModel = new LDAPConfigModel();
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapValidationAction.validate(emptyLDAPConfigModel);

        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertTrue(validationResponseModelActionResponse.getContent().isPresent());
        assertTrue(validationResponseModelActionResponse.getContent().get().hasErrors());
        assertEquals("There were problems with the configuration", validationResponseModelActionResponse.getContent().get().getMessage());
    }

    @Test
    void testValidateValidConfigModel() {
        LDAPConfigModel emptyLDAPConfigModel = new LDAPConfigModel();
        emptyLDAPConfigModel.setManagerPassword("managerPassword");
        emptyLDAPConfigModel.setManagerDn("managerDn");
        emptyLDAPConfigModel.setServerName("serverName");
        emptyLDAPConfigModel.setIsManagerPasswordSet(true);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapValidationAction.validate(emptyLDAPConfigModel);

        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertTrue(validationResponseModelActionResponse.getContent().isPresent());
        assertFalse(validationResponseModelActionResponse.getContent().get().hasErrors());
        assertEquals("The configuration is valid", validationResponseModelActionResponse.getContent().get().getMessage());
    }
}
