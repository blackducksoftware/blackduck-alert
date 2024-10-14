/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import com.blackduck.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.authentication.ldap.LDAPConfig;
import com.blackduck.integration.alert.authentication.ldap.LDAPTestHelper;
import com.blackduck.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;

public class LDAPManagerTest {
    private LDAPConfigAccessor ldapConfigAccessor;
    private LDAPManager ldapManager;

    private LDAPConfigModel validLDAPConfigModel;
    private LDAPConfigModel invalidLDAPConfigModel;

    @BeforeEach
    public void initAccessor() {
        ldapConfigAccessor = LDAPTestHelper.createTestLDAPConfigAccessor();
        UserManagementAuthoritiesPopulator mockUserManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ldapManager = new LDAPManager(ldapConfigAccessor, mockUserManagementAuthoritiesPopulator, new LDAPConfig().ldapUserContextMapper());

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
        invalidLDAPConfigModel = LDAPTestHelper.createInvalidLDAPConfigModel();
    }

    @Test
    public void testCreateConfiguration() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        assertFalse(ldapManager.isLDAPEnabled());

        validLDAPConfigModel.setAuthenticationType("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));

        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, expectedLDAPConfigModel.getName());
        assertTrue(ldapManager.isLDAPEnabled());

        assertNotEquals(LDAPTestHelper.DEFAULT_CONFIG_ID, expectedLDAPConfigModel.getId());
        assertTrue(expectedLDAPConfigModel.getEnabled());
        assertEquals(LDAPTestHelper.DEFAULT_SERVER_NAME, expectedLDAPConfigModel.getServerName());
        assertEquals(LDAPTestHelper.DEFAULT_MANAGER_DN, expectedLDAPConfigModel.getManagerDn());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_MANAGER_PASSWORD, expectedLDAPConfigModel::getManagerPassword);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_REFERRAL, expectedLDAPConfigModel::getReferral);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_USER_SEARCH_BASE, expectedLDAPConfigModel::getUserSearchBase);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_USER_SEARCH_FILTER, expectedLDAPConfigModel::getUserSearchFilter);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_USER_DN_PATTERNS, expectedLDAPConfigModel::getUserDnPatterns);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_USER_ATTRIBUTES, expectedLDAPConfigModel::getUserAttributes);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_GROUP_SEARCH_BASE, expectedLDAPConfigModel::getGroupSearchBase);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_GROUp_SEARCH_FILTER, expectedLDAPConfigModel::getGroupSearchFilter);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_GROUP_ROLE_ATTRIBUTES, expectedLDAPConfigModel::getGroupRoleAttribute);
    }

    @Test
    public void testConfigurationAlreadyExists() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        validLDAPConfigModel.setAuthenticationType("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));

        AlertConfigurationException alertConfigurationException = assertThrows(
            AlertConfigurationException.class,
            () -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel)
        );
        assertTrue(alertConfigurationException.getMessage().contains("An LDAP configuration already exists."));
        assertEquals(expectedLDAPConfigModel.getId(), ldapManager.getCurrentConfiguration().get().getId());
    }

    @Test
    public void testDeleteConfiguration() {
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
        validLDAPConfigModel.setAuthenticationType("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        assertTrue(ldapManager.getCurrentConfiguration().isPresent());
        ldapConfigAccessor.deleteConfiguration();
        assertFalse(ldapManager.getCurrentConfiguration().isPresent());
    }

    @Test
    public void testGetAuthenticationProviderCreated() {
        Optional<LdapAuthenticationProvider> ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertEquals(Optional.empty(), ldapAuthenticationProvider);
        validLDAPConfigModel.setAuthenticationType("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        assertTrue(ldapAuthenticationProvider.isPresent());
    }

    @Test
    public void testAuthenticationTypeNull() {
        validLDAPConfigModel.setAuthenticationType(null);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeNone() {
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_NONE);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_NONE, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeSimple() {
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeDigest() {
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testAuthenticationTypeUnsupported() {
        validLDAPConfigModel.setAuthenticationType("Unsupported authentication type");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        LDAPConfigModel expectedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Expected LDAPConfigModel did not exist"));
        assertDoesNotThrow(() -> ldapManager.getAuthenticationProvider());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, expectedLDAPConfigModel::getAuthenticationType);
    }

    @Test
    public void testCreateAuthProviderInvalidValues() {
        Optional<LdapAuthenticationProvider> ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.createAuthProvider(invalidLDAPConfigModel));
        assertEquals(Optional.empty(), ldapAuthenticationProvider);

        invalidLDAPConfigModel.setEnabled(true);
        ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.createAuthProvider(invalidLDAPConfigModel));
        assertEquals(Optional.empty(), ldapAuthenticationProvider);

        invalidLDAPConfigModel.setServerName("serverName");
        ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.createAuthProvider(invalidLDAPConfigModel));
        assertEquals(Optional.empty(), ldapAuthenticationProvider);

        invalidLDAPConfigModel.setManagerDn("managerDn");
        ldapAuthenticationProvider = assertDoesNotThrow(() -> ldapManager.createAuthProvider(invalidLDAPConfigModel));
        assertEquals(Optional.empty(), ldapAuthenticationProvider);
    }

    @Test
    public void testCreateAuthenticatorException() {
        validLDAPConfigModel.setAuthenticationType("");
        validLDAPConfigModel.setUserSearchBase("");
        validLDAPConfigModel.setUserSearchFilter("");

        AlertConfigurationException alertConfigurationException = assertThrows(AlertConfigurationException.class, () -> ldapManager.createAuthProvider(validLDAPConfigModel));
        assertTrue(alertConfigurationException.getMessage().contains("Error creating LDAP authenticator"));
    }

    @Test
    public void testUpdateConfigNotExist() {
        validLDAPConfigModel.setEnabled(false);
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        assertFalse(ldapConfigAccessor.doesConfigurationExist());
        AlertConfigurationException alertConfigurationException = assertThrows(
            AlertConfigurationException.class,
            () -> ldapConfigAccessor.updateConfiguration(validLDAPConfigModel)
        );
        assertTrue(alertConfigurationException.getMessage().contains("An LDAP configuration does not exist"));
        assertFalse(ldapConfigAccessor.doesConfigurationExist());
    }

    @Test
    public void testUpdateConfigWithPassword() {
        // Set up and verify initial LDAPConfigModel
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Raw input LDAPConfigModel does not exist"));
        assertEquals(true, retrievedInputLDAPConfigModel.getEnabled());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, retrievedInputLDAPConfigModel::getAuthenticationType);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_MANAGER_PASSWORD, retrievedInputLDAPConfigModel::getManagerPassword);

        // Set up and verify updated LDAPConfigModel
        // Verify updated values were in fact updated
        validLDAPConfigModel.setEnabled(false);
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        validLDAPConfigModel.setManagerPassword("My Password");
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(validLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertEquals(false, retrievedUpdatedLDAPConfigModel.getEnabled());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST, retrievedUpdatedLDAPConfigModel::getAuthenticationType);

        // Verify password was updated
        LDAPTestHelper.assertOptionalField("My Password", retrievedUpdatedLDAPConfigModel::getManagerPassword);

        // Verify we are still using the same LDAPConfigModel based on the ID
        assertEquals(retrievedInputLDAPConfigModel.getId(), retrievedUpdatedLDAPConfigModel.getId());
    }

    @Test
    public void testUpdateConfigWithOutPassword() {
        // Set up and verify initial LDAPConfigModel
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Input LDAPConfigModel does not exist"));
        assertEquals(true, retrievedInputLDAPConfigModel.getEnabled());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_SIMPLE, retrievedInputLDAPConfigModel::getAuthenticationType);
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_MANAGER_PASSWORD, retrievedInputLDAPConfigModel::getManagerPassword);

        // Set up and verify updated LDAPConfigModel
        // Verify updated values were in fact updated
        validLDAPConfigModel.setEnabled(false);
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        validLDAPConfigModel.setManagerPassword(null);
        validLDAPConfigModel.setIsManagerPasswordSet(true);
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(validLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel does not exist"));
        assertEquals(false, retrievedUpdatedLDAPConfigModel.getEnabled());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST, retrievedUpdatedLDAPConfigModel::getAuthenticationType);

        // Verify password is same as input ConfigModel
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_MANAGER_PASSWORD, retrievedUpdatedLDAPConfigModel::getManagerPassword);

        // Verify we are still using the same LDAPConfigModel based on the ID
        assertEquals(retrievedInputLDAPConfigModel.getId(), retrievedUpdatedLDAPConfigModel.getId());
    }

    @Test
    public void testUpdateConfigEmptyPassword() {
        // Set up and verify initial LDAPConfigModel
        validLDAPConfigModel.setManagerPassword(null);
        validLDAPConfigModel.setIsManagerPasswordSet(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validLDAPConfigModel));
        assertTrue(ldapConfigAccessor.doesConfigurationExist());
        LDAPConfigModel retrievedInputLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Input LDAPConfigModel does not exist"));
        assertEquals("", retrievedInputLDAPConfigModel.getManagerPassword().orElse(""));

        // Set up and verify updated LDAPConfigModel
        // Verify password is still empty
        validLDAPConfigModel.setEnabled(false);
        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        validLDAPConfigModel.setManagerPassword(null);
        validLDAPConfigModel.setIsManagerPasswordSet(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.updateConfiguration(validLDAPConfigModel));
        LDAPConfigModel retrievedUpdatedLDAPConfigModel = ldapManager.getCurrentConfiguration()
            .orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel does not exist"));
        assertEquals("", retrievedUpdatedLDAPConfigModel.getManagerPassword().orElse(""));
    }

}
