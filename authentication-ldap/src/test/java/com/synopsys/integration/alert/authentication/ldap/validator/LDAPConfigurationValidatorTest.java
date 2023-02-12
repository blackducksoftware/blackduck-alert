package com.synopsys.integration.alert.authentication.ldap.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;

public class LDAPConfigurationValidatorTest {
    private final LDAPConfigModel ldapConfigModel = new LDAPConfigModel();
    private final LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

    @Test
    public void testNewValidModel() {
        ldapConfigModel.setServerName("valid");
        ldapConfigModel.setManagerDn("valid");
        ldapConfigModel.setManagerPassword("valid");
        ldapConfigModel.setIsManagerPasswordSet(true);

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_SUCCESS_MESSAGE, validationResponseModel.getMessage());
        assertEquals(0, validationResponseModel.getErrors().size());
    }

    @Test
    public void testUpdatedValidModel() {
        // The scenario can come up when updating a configuration via the UI
        //   ldapConfigModel.getManagerPassword().isEmpty() = true
        //   ldapConfigModel.getIsManagerPasswordSet() = true

        ldapConfigModel.setServerName("valid");
        ldapConfigModel.setManagerDn("valid");
        ldapConfigModel.setManagerPassword("valid");
        ldapConfigModel.setIsManagerPasswordSet(true);
        LDAPConfigModel obfuscatedLDAPConfigModel = ldapConfigModel.obfuscate();

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(obfuscatedLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_SUCCESS_MESSAGE, validationResponseModel.getMessage());
        assertEquals(0, validationResponseModel.getErrors().size());
    }

    @Test
    public void testInvalidServerName() {
        ldapConfigModel.setManagerDn("valid");
        ldapConfigModel.setManagerPassword("valid");
        ldapConfigModel.setIsManagerPasswordSet(true);

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("serverName"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("serverName").getFieldMessage());
    }

    @Test
    public void testInvalidManagerDn() {
        ldapConfigModel.setServerName("valid");
        ldapConfigModel.setManagerPassword("valid");
        ldapConfigModel.setIsManagerPasswordSet(true);

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("managerDn"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("managerDn").getFieldMessage());
    }

    @Test
    public void testInvalidManagerPassword() {
        ldapConfigModel.setServerName("valid");
        ldapConfigModel.setManagerDn("valid");
        ldapConfigModel.setIsManagerPasswordSet(false);

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("managerPassword"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("managerPassword").getFieldMessage());
    }

}
