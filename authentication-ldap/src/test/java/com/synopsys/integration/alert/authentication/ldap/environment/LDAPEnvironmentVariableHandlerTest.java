package com.synopsys.integration.alert.authentication.ldap.environment;

import static com.synopsys.integration.alert.authentication.ldap.environment.LDAPEnvironmentVariableHandler.LDAP_CONFIGURATION_KEY_SET;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.mock.env.MockEnvironment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.authentication.ldap.LDAPTestHelper;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;

class LDAPEnvironmentVariableHandlerTest {
    private LDAPConfigModel validLDAPConfigModel;

    MockEnvironment mockEnvironment;

    LDAPConfigAccessor ldapConfigAccessor;
    LDAPEnvironmentVariableHandler ldapEnvironmentVariableHandler;

    @BeforeEach
    void initEach() {
        ldapConfigAccessor = LDAPTestHelper.createTestLDAPConfigAccessor();
        LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

        mockEnvironment = new MockEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        ldapEnvironmentVariableHandler = new LDAPEnvironmentVariableHandler(
            ldapConfigAccessor,
            ldapConfigurationValidator,
            environmentVariableUtility
        );

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
    }

    @Test
    void testConfigurationMissingCheck() {
        assertTrue(ldapEnvironmentVariableHandler.configurationMissingCheck());
        ldapEnvironmentVariableHandler.saveConfiguration(validLDAPConfigModel, null);
        assertFalse(ldapEnvironmentVariableHandler.configurationMissingCheck());
    }

    @Test
    void testEnvironmentValuesNonePresent() {
        LDAPConfigModel ldapConfigModel = ldapEnvironmentVariableHandler.configureModel();

        assertNull(ldapConfigModel.getName());
        assertNull(ldapConfigModel.getId());

        assertNotNull(ldapConfigModel.getCreatedAt());
        assertNotNull(ldapConfigModel.getLastUpdated());

        assertTrue(ldapConfigModel.getAuthenticationType().isEmpty());
        assertFalse(ldapConfigModel.getEnabled());
        assertTrue(ldapConfigModel.getGroupRoleAttribute().isEmpty());
        assertTrue(ldapConfigModel.getGroupSearchBase().isEmpty());
        assertTrue(ldapConfigModel.getGroupSearchFilter().isEmpty());
        assertNull(ldapConfigModel.getManagerDn());
        assertTrue(ldapConfigModel.getManagerPassword().isEmpty());
        assertFalse(ldapConfigModel.getIsManagerPasswordSet());
        assertTrue(ldapConfigModel.getReferral().isEmpty());
        assertNull(ldapConfigModel.getServerName());
        assertTrue(ldapConfigModel.getUserAttributes().isEmpty());
        assertTrue(ldapConfigModel.getUserDnPatterns().isEmpty());
        assertTrue(ldapConfigModel.getUserSearchBase().isEmpty());
        assertTrue(ldapConfigModel.getUserSearchFilter().isEmpty());
    }

    @Test
    void testEnvironmentValuesAllPresent() {
        LDAPConfigModel ldapConfigModel = createValidConfigModelFromEnvironment();

        assertNull(ldapConfigModel.getName());
        assertNull(ldapConfigModel.getId());
        assertNotNull(ldapConfigModel.getCreatedAt());
        assertNotNull(ldapConfigModel.getLastUpdated());

        assertFalse(ldapConfigModel.getEnabled());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_MANAGER_DN_KEY, ldapConfigModel.getManagerDn());
        assertTrue(ldapConfigModel.getIsManagerPasswordSet());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_SERVER_KEY, ldapConfigModel.getServerName());

        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, ldapConfigModel::getAuthenticationType);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_GROUP_ROLE_ATTRIBUTE_KEY, ldapConfigModel::getGroupRoleAttribute);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_BASE_KEY, ldapConfigModel::getGroupSearchBase);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_FILTER_KEY, ldapConfigModel::getGroupSearchFilter);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY, ldapConfigModel::getManagerPassword);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_REFERRAL_KEY, ldapConfigModel::getReferral);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_USER_ATTRIBUTES_KEY, ldapConfigModel::getUserAttributes);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_USER_DN_PATTERNS_KEY, ldapConfigModel::getUserDnPatterns);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_BASE_KEY, ldapConfigModel::getUserSearchBase);
        LDAPTestHelper.assertOptionalField(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_FILTER_KEY, ldapConfigModel::getUserSearchFilter);
    }

    @Test
    void testValidateConfigurationContainedErrors() {
        LDAPConfigModel ldapConfigModel = ldapEnvironmentVariableHandler.configureModel();

        ValidationResponseModel validationResponseModel = ldapEnvironmentVariableHandler.validateConfiguration(ldapConfigModel);
        assertTrue(validationResponseModel.hasErrors());

        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testUpdateNoConfigurationNoEnvironment() {
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testUpdateNoConfigurationValidEnvironment() {
        createValidConfigModelFromEnvironment();
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertNotEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testUpdateValidConfigurationNoEnvironment() {
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testUpdateValidConfigurationValidEnvironment() {
        createValidConfigModelFromEnvironment();
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testBuildProcessingResultAllValuesSet() {
        LDAPConfigModel ldapConfigModel = createValidConfigModelFromEnvironment();
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.buildProcessingResult(ldapConfigModel.obfuscate());
        assertTrue(environmentProcessingResult.getVariableNames().containsAll(LDAP_CONFIGURATION_KEY_SET));

        // The values for PW, ENABLED, and auth-type will not be the same as the key, so handle separately
        LDAPTestHelper.assertOptionalField(
            AlertConstants.MASKED_VALUE,
            () -> environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY)
        );
        LDAPTestHelper.assertOptionalField("false", () -> environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_ENABLED_KEY));
        LDAPTestHelper.assertOptionalField(
            LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE,
            () -> environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_AUTHENTICATION_TYPE_KEY)
        );

        Set<String> valuesToValidate = new java.util.HashSet<>(LDAP_CONFIGURATION_KEY_SET);
        valuesToValidate.remove(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY);
        valuesToValidate.remove(LDAPEnvironmentVariableHandler.LDAP_ENABLED_KEY);
        valuesToValidate.remove(LDAPEnvironmentVariableHandler.LDAP_AUTHENTICATION_TYPE_KEY);

        // The values for all the remaining keys should be the same as the key
        for (String variableName : valuesToValidate) {
            LDAPTestHelper.assertOptionalField(variableName, () -> environmentProcessingResult.getVariableValue(variableName));
        }
    }

    @Test
    void testBuildProcessingResultRequiredValuesUnset() {
        LDAPConfigModel ldapConfigModel = createValidConfigModelFromEnvironment();
        ldapConfigModel.setServerName("");
        ldapConfigModel.setManagerDn("");
        ldapConfigModel.setManagerPassword("");
        ldapConfigModel.setIsManagerPasswordSet(false);
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.buildProcessingResult(ldapConfigModel.obfuscate());
        assertTrue(environmentProcessingResult.getVariableNames().containsAll(LDAP_CONFIGURATION_KEY_SET));

        assertTrue(environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_MANAGER_DN_KEY).isEmpty());
        assertTrue(environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY).isEmpty());
        assertTrue(environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_SERVER_KEY).isEmpty());
    }

    @Test
    void testSaveConfigurationAlreadyExists() {
        ldapEnvironmentVariableHandler.saveConfiguration(validLDAPConfigModel, null);
        assertFalse(ldapEnvironmentVariableHandler.configurationMissingCheck());
        LDAPConfigModel expectedLDAPConfigModel = ldapConfigAccessor.getConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);

        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        ldapEnvironmentVariableHandler.saveConfiguration(validLDAPConfigModel, null);
        LDAPConfigModel updatedLDAPConfigModel = ldapConfigAccessor.getConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));

        // Because it already existed, the Authentication Type should not have been updated to the new value
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, updatedLDAPConfigModel::getAuthenticationType);
    }

    private LDAPConfigModel createValidConfigModelFromEnvironment() {
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_AUTHENTICATION_TYPE_KEY, LDAPEnvironmentVariableHandler.LDAP_AUTHENTICATION_TYPE_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_ENABLED_KEY, "false");
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_GROUP_ROLE_ATTRIBUTE_KEY, LDAPEnvironmentVariableHandler.LDAP_GROUP_ROLE_ATTRIBUTE_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_BASE_KEY, LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_BASE_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_FILTER_KEY, LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_FILTER_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_MANAGER_DN_KEY, LDAPEnvironmentVariableHandler.LDAP_MANAGER_DN_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY, LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_REFERRAL_KEY, LDAPEnvironmentVariableHandler.LDAP_REFERRAL_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_SERVER_KEY, LDAPEnvironmentVariableHandler.LDAP_SERVER_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_USER_ATTRIBUTES_KEY, LDAPEnvironmentVariableHandler.LDAP_USER_ATTRIBUTES_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_USER_DN_PATTERNS_KEY, LDAPEnvironmentVariableHandler.LDAP_USER_DN_PATTERNS_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_BASE_KEY, LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_BASE_KEY);
        mockEnvironment.setProperty(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_FILTER_KEY, LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_FILTER_KEY);
        return ldapEnvironmentVariableHandler.configureModel();
    }
}
