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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.mock.env.MockEnvironment;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class LDAPEnvironmentVariableHandlerTest {
    private static final LDAPConfigModel basicLDAPConfigModel = new LDAPConfigModel(UUID.randomUUID().toString(), "serverName", "managerDn", "managerPassword");

    MockEnvironment mockEnvironment;

    LDAPConfigAccessor ldapConfigAccessor;
    LDAPEnvironmentVariableHandler ldapEnvironmentVariableHandler;

    @BeforeEach
    void initEach() {
        Gson gson = new Gson();
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);
        LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

        mockEnvironment = new MockEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        ldapEnvironmentVariableHandler = new LDAPEnvironmentVariableHandler(
            ldapConfigAccessor,
            ldapConfigurationValidator,
            environmentVariableUtility
        );
    }

    @Test
    void testConfigurationMissingCheck() {
        assertTrue(ldapEnvironmentVariableHandler.configurationMissingCheck());
        ldapEnvironmentVariableHandler.saveConfiguration(basicLDAPConfigModel, null);
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

        assertEquals(LDAPEnvironmentVariableHandler.LDAP_AUTHENTICATION_TYPE_KEY, ldapConfigModel.getAuthenticationType().orElse(""));
        assertFalse(ldapConfigModel.getEnabled());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_GROUP_ROLE_ATTRIBUTE_KEY, ldapConfigModel.getGroupRoleAttribute().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_BASE_KEY, ldapConfigModel.getGroupSearchBase().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_GROUP_SEARCH_FILTER_KEY, ldapConfigModel.getGroupSearchFilter().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_MANAGER_DN_KEY, ldapConfigModel.getManagerDn());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY, ldapConfigModel.getManagerPassword().orElse(""));
        assertTrue(ldapConfigModel.getIsManagerPasswordSet());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_REFERRAL_KEY, ldapConfigModel.getReferral().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_SERVER_KEY, ldapConfigModel.getServerName());
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_USER_ATTRIBUTES_KEY, ldapConfigModel.getUserAttributes().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_USER_DN_PATTERNS_KEY, ldapConfigModel.getUserDnPatterns().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_BASE_KEY, ldapConfigModel.getUserSearchBase().orElse(""));
        assertEquals(LDAPEnvironmentVariableHandler.LDAP_USER_SEARCH_FILTER_KEY, ldapConfigModel.getUserSearchFilter().orElse(""));
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
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(basicLDAPConfigModel));
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testUpdateValidConfigurationValidEnvironment() {
        createValidConfigModelFromEnvironment();
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(basicLDAPConfigModel));
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EnvironmentProcessingResult.empty(), environmentProcessingResult);
    }

    @Test
    void testBuildProcessingResultAllValuesSet() {
        LDAPConfigModel ldapConfigModel = createValidConfigModelFromEnvironment();
        EnvironmentProcessingResult environmentProcessingResult = ldapEnvironmentVariableHandler.buildProcessingResult(ldapConfigModel.obfuscate());
        assertTrue(environmentProcessingResult.getVariableNames().containsAll(LDAP_CONFIGURATION_KEY_SET));

        // The values for PW and ENABLED will not be the same as the key, so handle separately
        assertEquals(AlertConstants.MASKED_VALUE, environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY).orElse(""));
        assertEquals("false", environmentProcessingResult.getVariableValue(LDAPEnvironmentVariableHandler.LDAP_ENABLED_KEY).orElse(""));

        Set<String> valuesToValidate = new java.util.HashSet<>(LDAP_CONFIGURATION_KEY_SET);
        valuesToValidate.remove(LDAPEnvironmentVariableHandler.LDAP_MANAGER_PASSWORD_KEY);
        valuesToValidate.remove(LDAPEnvironmentVariableHandler.LDAP_ENABLED_KEY);

        // The values for all the remaining keys should be the same as the key
        for (String variableName : valuesToValidate) {
            assertEquals(variableName, environmentProcessingResult.getVariableValue(variableName).orElse(""));
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
        ldapEnvironmentVariableHandler.saveConfiguration(basicLDAPConfigModel, null);
        assertFalse(ldapEnvironmentVariableHandler.configurationMissingCheck());
        LDAPConfigModel expectedLDAPConfigModel = ldapConfigAccessor.getConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertEquals("", expectedLDAPConfigModel.getAuthenticationType().orElse("FAIL"));

        basicLDAPConfigModel.setAuthenticationType("AUTH-TYPE");
        ldapEnvironmentVariableHandler.saveConfiguration(basicLDAPConfigModel, null);
        LDAPConfigModel updatedLDAPConfigModel = ldapConfigAccessor.getConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));

        // Because it already existed, the Authentication Type should not have been updated to the new value
        assertEquals("", updatedLDAPConfigModel.getAuthenticationType().orElse("FAIL"));
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
