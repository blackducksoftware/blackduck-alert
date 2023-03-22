package com.synopsys.integration.alert.authentication.ldap.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.ldap.LDAPTestHelper;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;

public class LDAPConfigurationValidatorTest {
    public static final String TEST_USER = "testLDAPUsername";
    public static final String TEST_PASS = "testLDAPPassword";

    private LDAPConfigModel validLDAPConfigModel;
    private LDAPConfigurationValidator ldapConfigurationValidator;

    @BeforeEach
    public void init() {
        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
        ldapConfigurationValidator = new LDAPConfigurationValidator();
    }

    @Test
    public void testNewValidConfigModel() {
        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(validLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_SUCCESS_MESSAGE, validationResponseModel.getMessage());
        assertEquals(0, validationResponseModel.getErrors().size());
    }

    @Test
    public void testUpdatedValidConfigModel() {
        // The scenario can come up when updating a configuration via the UI
        //   ldapConfigModel.getManagerPassword().isEmpty() = true
        //   ldapConfigModel.getIsManagerPasswordSet() = true

        LDAPConfigModel obfuscatedLDAPConfigModel = validLDAPConfigModel.obfuscate();

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(obfuscatedLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_SUCCESS_MESSAGE, validationResponseModel.getMessage());
        assertEquals(0, validationResponseModel.getErrors().size());
    }

    @Test
    public void testInvalidServerName() {
        validLDAPConfigModel.setServerName("");

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(validLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("serverName"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("serverName").getFieldMessage());
    }

    @Test
    public void testInvalidManagerDn() {
        validLDAPConfigModel.setManagerDn("");

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(validLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("managerDn"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("managerDn").getFieldMessage());
    }

    @Test
    public void testInvalidManagerPassword() {
        validLDAPConfigModel.setManagerPassword("");
        validLDAPConfigModel.setIsManagerPasswordSet(false);

        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(validLDAPConfigModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(2, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("managerPassword"));
        assertEquals(AlertFieldStatusMessages.INVALID_OPTION, validationResponseModel.getErrors().get("isManagerPasswordSet").getFieldMessage());
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("managerPassword").getFieldMessage());
    }

    @Test
    public void testNullConfigModel() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(null, TEST_USER, TEST_PASS);
        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigTestModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey("ldapConfigModel"));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get("ldapConfigModel").getFieldMessage());
    }

    @Test
    public void testEmptyConfigModel() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(LDAPTestHelper.createInvalidLDAPConfigModel(), TEST_USER, TEST_PASS);
        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigTestModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(4, validationResponseModel.getErrors().size());
    }

    @Test
    public void testInvalidTestUser() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, "", null);
        ValidationResponseModel validationResponseModel = ldapConfigurationValidator.validate(ldapConfigTestModel);
        assertEquals(ValidationResponseModel.VALIDATION_FAILURE_MESSAGE, validationResponseModel.getMessage());
        assertEquals(2, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(TEST_USER));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get(TEST_USER).getFieldMessage());
        assertTrue(validationResponseModel.getErrors().containsKey(TEST_PASS));
        assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, validationResponseModel.getErrors().get(TEST_PASS).getFieldMessage());
    }
}
