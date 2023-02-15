package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

public class LdapManagerTest {
    private static final String DEFAULT_CONFIG_ID = UUID.randomUUID().toString();
    private static final String DEFAULT_DATE_STRING = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
    private static final String DEFAULT_AUTHENTICATION_TYPE_SIMPLE = "simple";
    private static final String DEFAULT_AUTHENTICATION_TYPE_DIGEST = "digest";
    private static final String DEFAULT_GROUP_ROLE_ATTRIBUTE = "groupRoleAttribute";
    private static final String DEFAULT_GROUP_SEARCH_BASE = "groupSearchBase";
    private static final String DEFAULT_GROUP_SEARCH_FILTER = "groupSearchFilter";
    private static final String DEFAULT_MANAGER_DN = "managerDN";
    private static final String DEFAULT_MANAGER_PASSWORD = "managerPassword";
    private static final String DEFAULT_REFERRAL = "referral";
    private static final String DEFAULT_SERVER = "aserver";
    private static final String DEFAULT_USER_ATTRIBUTES = "userAttribute1,userAttribute2";
    private static final String DEFAULT_USER_DN_PATTERNS = "userDNPattern1,userDNPattern2";
    private static final String DEFAULT_USER_SEARCH_BASE = "userSearchbase";
    private static final String DEFAULT_USER_SEARCH_FILTER = "userSearchFilter";

    private static final InetOrgPersonContextMapper LDAP_USER_CONTEXT_MAPPER = new InetOrgPersonContextMapper();

    private LDAPConfigAccessor ldapConfigAccessor;
    private LdapManager ldapManager;

    @BeforeEach
    public void initAccessor() {
        // Create new LDAPConfigAccessor
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);

        // Create new LdapManager
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ldapManager = new LdapManager(ldapConfigAccessor, userManagementAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
    }

    @Test
    public void testCreateConfiguration() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        assertFalse(ldapManager.isLdapEnabled());
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, "");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertTrue(ldapManager.getCurrentConfiguration().isPresent());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, expectedLDAPConfigModel.getName());
        assertTrue(ldapManager.isLdapEnabled());
        assertNotEquals(DEFAULT_CONFIG_ID, expectedLDAPConfigModel.getId());
        assertEquals(true, expectedLDAPConfigModel.getEnabled());
        assertEquals(DEFAULT_SERVER, expectedLDAPConfigModel.getServerName());
        assertEquals(DEFAULT_MANAGER_DN, expectedLDAPConfigModel.getManagerDn());
        assertTrue(expectedLDAPConfigModel.getManagerPassword().isPresent());
        assertOptionalField(DEFAULT_MANAGER_PASSWORD, expectedLDAPConfigModel::getManagerPassword);
        assertEquals("", expectedLDAPConfigModel.getAuthenticationType().orElse(""));
        assertOptionalField(DEFAULT_REFERRAL, expectedLDAPConfigModel::getReferral);
        assertOptionalField(DEFAULT_USER_SEARCH_BASE, expectedLDAPConfigModel::getUserSearchBase);
        assertOptionalField(DEFAULT_USER_SEARCH_FILTER, expectedLDAPConfigModel::getUserSearchFilter);
        assertOptionalField(DEFAULT_USER_DN_PATTERNS, expectedLDAPConfigModel::getUserDnPatterns);
        assertOptionalField(DEFAULT_USER_ATTRIBUTES, expectedLDAPConfigModel::getUserAttributes);
        assertOptionalField(DEFAULT_GROUP_SEARCH_BASE, expectedLDAPConfigModel::getGroupSearchBase);
        assertOptionalField(DEFAULT_GROUP_SEARCH_FILTER, expectedLDAPConfigModel::getGroupSearchFilter);
        assertOptionalField(DEFAULT_GROUP_ROLE_ATTRIBUTE, expectedLDAPConfigModel::getGroupRoleAttribute);
    }

    @Test
    public void testConfigurationExists() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, "");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        Optional<LDAPConfigModel> expectedLDAPConfigModel = ldapManager.getCurrentConfiguration();
        assertTrue(ldapManager.getCurrentConfiguration().isPresent());

        AlertConfigurationException alertConfigurationException = assertThrows(AlertConfigurationException.class, () -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        assertTrue(alertConfigurationException.getMessage().contains("An LDAP configuration already exists."));
        assertEquals(expectedLDAPConfigModel.get().getId(), ldapManager.getCurrentConfiguration().get().getId());
    }

    @Test
    public void testDeleteConfiguration() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, "");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        assertTrue(ldapManager.getCurrentConfiguration().isPresent());
        ldapConfigAccessor.deleteConfiguration();
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
    }

    @Test
    public void testGetAuthenticationProviderCreated() {
        Optional<LdapAuthenticationProvider> ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertEquals(Optional.empty(), ldapAuthenticationProvider);
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, "");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertTrue(ldapAuthenticationProvider.isPresent());
    }

    @Test
    public void testAuthenticationTypeNull() {
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, null);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertEquals("", expectedLDAPConfigModel.getAuthenticationType().orElse(""));
    }

    @Test
    public void testAuthenticationTypeSimple() {
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, DEFAULT_AUTHENTICATION_TYPE_SIMPLE);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeDigest() {
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, DEFAULT_AUTHENTICATION_TYPE_DIGEST);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_DIGEST, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeUnsupported() {
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(true, "Unsupported authentication type");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertOptionalField("Unsupported authentication type", expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testCreateAuthProviderDisabled() throws AlertConfigurationException {
        LDAPConfigModel ldapConfigModel = createLDAPConfigModel(false, DEFAULT_AUTHENTICATION_TYPE_DIGEST);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));
        Optional<LdapAuthenticationProvider> expectedLDAPAuthenticationProvider = ldapManager.createAuthProvider(ldapConfigModel);
        assertEquals(Optional.empty(), expectedLDAPAuthenticationProvider);
    }

    @Test
    public void testExceptionOnContext() {
        LDAPConfigModel invalidConfigModel = new LDAPConfigModel("", "", "", true, "", "", "", false, "", "", "", "", "", "", "", "", "");
        AlertConfigurationException alertConfigurationException = assertThrows(AlertConfigurationException.class, () -> ldapManager.createAuthProvider(invalidConfigModel));
        assertTrue(alertConfigurationException.getMessage().contains("Error creating LDAP Context Source"));
    }

    @Test
    public void testCreateAuthenticatorException() {
        LDAPConfigModel ldapConfigModel = new LDAPConfigModel(
            DEFAULT_CONFIG_ID,
            DEFAULT_DATE_STRING,
            DEFAULT_DATE_STRING,
            true,
            DEFAULT_SERVER,
            DEFAULT_MANAGER_DN,
            DEFAULT_MANAGER_PASSWORD,
            StringUtils.isNotBlank(DEFAULT_MANAGER_PASSWORD),
            "",
            DEFAULT_REFERRAL,
            "",
            "",
            "",
            DEFAULT_USER_ATTRIBUTES,
            DEFAULT_GROUP_SEARCH_BASE,
            DEFAULT_GROUP_SEARCH_FILTER,
            DEFAULT_GROUP_ROLE_ATTRIBUTE
        );

        AlertConfigurationException alertConfigurationException = assertThrows(AlertConfigurationException.class, () -> ldapManager.createAuthProvider(ldapConfigModel));
        assertTrue(alertConfigurationException.getMessage().contains("Error creating LDAP authenticator"));
    }

    @Test
    public void testUpdateConfigNotExist() {
        assertFalse(ldapConfigAccessor.doesConfigurationExist());
        AlertConfigurationException alertConfigurationException = assertThrows(
            AlertConfigurationException.class,
            () -> ldapConfigAccessor.updateConfiguration(createLDAPConfigModel(false, DEFAULT_AUTHENTICATION_TYPE_DIGEST))
        );
        assertTrue(alertConfigurationException.getMessage().contains("An LDAP configuration does not exist"));
        assertFalse(ldapConfigAccessor.doesConfigurationExist());
    }

    @Test
    public void testUpdateConfigWithPassword() {
        // Set up and verify initial LDAPConfigModel
        LDAPConfigModel inputLDAPConfigModel = createLDAPConfigModel(true, DEFAULT_AUTHENTICATION_TYPE_SIMPLE);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(inputLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Raw input LDAPConfigModel does not exist"));
        assertEquals(true, retrievedInputLDAPConfigModel.getEnabled());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_SIMPLE, retrievedInputLDAPConfigModel::getAuthenticationType);
        assertOptionalField(DEFAULT_MANAGER_PASSWORD, retrievedInputLDAPConfigModel::getManagerPassword);

        // Set up and verify updated LDAPConfigModel
        // Verify updated values were in fact updated
        LDAPConfigModel updatedLDAPConfigModel = createLDAPConfigModel(false, DEFAULT_AUTHENTICATION_TYPE_DIGEST);
        updatedLDAPConfigModel.setManagerPassword("My Password");
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(updatedLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertEquals(false, retrievedUpdatedLDAPConfigModel.getEnabled());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_DIGEST, retrievedUpdatedLDAPConfigModel::getAuthenticationType);

        // Verify password was updated
        assertOptionalField("My Password", retrievedUpdatedLDAPConfigModel::getManagerPassword);

        // Verify we are still using the same LDAPConfigModel based on the ID
        assertEquals(retrievedInputLDAPConfigModel.getId(), retrievedUpdatedLDAPConfigModel.getId());
    }

    @Test
    public void testUpdateConfigWithOutPassword() {
        // Set up and verify initial LDAPConfigModel
        LDAPConfigModel inputLDAPConfigModel = createLDAPConfigModel(true, DEFAULT_AUTHENTICATION_TYPE_SIMPLE);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(inputLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Input LDAPConfigModel does not exist"));
        assertEquals(true, retrievedInputLDAPConfigModel.getEnabled());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_SIMPLE, retrievedInputLDAPConfigModel::getAuthenticationType);
        assertOptionalField(DEFAULT_MANAGER_PASSWORD, retrievedInputLDAPConfigModel::getManagerPassword);

        // Set up and verify updated LDAPConfigModel
        // Verify updated values were in fact updated
        LDAPConfigModel updatedLDAPConfigModel = createLDAPConfigModel(false, DEFAULT_AUTHENTICATION_TYPE_DIGEST);
        updatedLDAPConfigModel.setManagerPassword(null);
        updatedLDAPConfigModel.setIsManagerPasswordSet(true);
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(updatedLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel does not exist"));
        assertEquals(false, retrievedUpdatedLDAPConfigModel.getEnabled());
        assertOptionalField(DEFAULT_AUTHENTICATION_TYPE_DIGEST, retrievedUpdatedLDAPConfigModel::getAuthenticationType);

        // Verify password is same as input ConfigModel
        assertOptionalField(DEFAULT_MANAGER_PASSWORD, retrievedUpdatedLDAPConfigModel::getManagerPassword);

        // Verify we are still using the same LDAPConfigModel based on the ID
        assertEquals(retrievedInputLDAPConfigModel.getId(), retrievedUpdatedLDAPConfigModel.getId());
    }

    @Test
    public void testUpdateConfigEmptyPassword() {
        // Set up and verify initial LDAPConfigModel
        LDAPConfigModel inputLDAPConfigModel = createLDAPConfigModel(true, DEFAULT_AUTHENTICATION_TYPE_SIMPLE);
        inputLDAPConfigModel.setManagerPassword(null);
        inputLDAPConfigModel.setIsManagerPasswordSet(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(inputLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Input LDAPConfigModel does not exist"));
        assertEquals("", retrievedInputLDAPConfigModel.getManagerPassword().orElse(""));

        // Set up and verify updated LDAPConfigModel
        // Verify password is still empty
        LDAPConfigModel updatedLDAPConfigModel = createLDAPConfigModel(false, DEFAULT_AUTHENTICATION_TYPE_DIGEST);
        updatedLDAPConfigModel.setManagerPassword(null);
        updatedLDAPConfigModel.setIsManagerPasswordSet(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(updatedLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel does not exist"));
        assertEquals("", retrievedUpdatedLDAPConfigModel.getManagerPassword().orElse(""));
    }

    private LDAPConfigModel createLDAPConfigModel(Boolean enabled, String authenticationType) {
        return new LDAPConfigModel(
            DEFAULT_CONFIG_ID,
            DEFAULT_DATE_STRING,
            DEFAULT_DATE_STRING,
            enabled,
            DEFAULT_SERVER,
            DEFAULT_MANAGER_DN,
            DEFAULT_MANAGER_PASSWORD,
            StringUtils.isNotBlank(DEFAULT_MANAGER_PASSWORD),
            authenticationType,
            DEFAULT_REFERRAL,
            DEFAULT_USER_SEARCH_BASE,
            DEFAULT_USER_SEARCH_FILTER,
            DEFAULT_USER_DN_PATTERNS,
            DEFAULT_USER_ATTRIBUTES,
            DEFAULT_GROUP_SEARCH_BASE,
            DEFAULT_GROUP_SEARCH_FILTER,
            DEFAULT_GROUP_ROLE_ATTRIBUTE
        );
    }

    private void assertOptionalField(String expectedValue, Supplier<Optional<String>> fieldSupplier) {
        Optional<String> optional = fieldSupplier.get();
        assertTrue(optional.isPresent());
        assertEquals(expectedValue, optional.get());
    }

}
