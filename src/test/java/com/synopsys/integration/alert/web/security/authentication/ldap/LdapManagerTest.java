package com.synopsys.integration.alert.web.security.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;

public class LdapManagerTest {

    public static final String DEFAULT_ENABLED = "true";
    public static final String DEFAULT_SERVER = "aserver";
    public static final String DEFAULT_MANAGER_DN = "managerDN";
    public static final String DEFAULT_MANAGER_PASSWORD = "managerPassword";
    public static final String DEFAULT_AUTHENTICATION_TYPE = "simple";
    public static final String DEFAULT_REFERRAL = "referral";
    public static final String DEFAULT_USER_SEARCH_BASE = "searchbase";
    public static final String DEFAULT_USER_SEARCH_FILTER = "searchFilter";
    public static final String DEFAULT_USER_DN_PATTERNS = "pattern1,pattern2";
    public static final String DEFAULT_USER_ATTRIBUTES = "attribute1,attribute2";
    public static final String DEFAULT_GROUP_SEARCH_BASE = "groupSearchBase";
    public static final String DEFAULT_GROUP_SEARCH_FILTER = "groupSearchFilter";
    public static final String DEFAULT_GROUP_ROLE_ATTRIBUTE = "roleAttribute";
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    private ConfigurationModel createConfigurationModel() {
        final ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, ConfigContextEnum.GLOBAL);

        final ConfigurationFieldModel enabledField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_ENABLED);
        final ConfigurationFieldModel serverField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_SERVER);
        final ConfigurationFieldModel managerDNField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_MANAGER_DN);
        final ConfigurationFieldModel managerPasswordField = ConfigurationFieldModel.createSensitive(SettingsDescriptor.KEY_LDAP_MANAGER_PWD);
        final ConfigurationFieldModel authenticationTypeField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE);
        final ConfigurationFieldModel referralField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_REFERRAL);
        final ConfigurationFieldModel userSearchBaseField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        final ConfigurationFieldModel userSearchFilterField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        final ConfigurationFieldModel userDNPatternsField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS);
        final ConfigurationFieldModel userAttributesField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_USER_ATTRIBUTES);
        final ConfigurationFieldModel groupSearchBaseField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_BASE);
        final ConfigurationFieldModel groupSearchFilterField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER);
        final ConfigurationFieldModel groupRoleAttributeField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE);
        final ConfigurationFieldModel rolePrefixField = ConfigurationFieldModel.create(SettingsDescriptor.KEY_LDAP_ROLE_PREFIX);

        enabledField.setFieldValue(DEFAULT_ENABLED);
        serverField.setFieldValue(DEFAULT_SERVER);
        managerDNField.setFieldValue(DEFAULT_MANAGER_DN);
        managerPasswordField.setFieldValue(DEFAULT_MANAGER_PASSWORD);
        authenticationTypeField.setFieldValue(DEFAULT_AUTHENTICATION_TYPE);
        referralField.setFieldValue(DEFAULT_REFERRAL);
        userSearchBaseField.setFieldValue(DEFAULT_USER_SEARCH_BASE);
        userSearchFilterField.setFieldValue(DEFAULT_USER_SEARCH_FILTER);
        userDNPatternsField.setFieldValue(DEFAULT_USER_DN_PATTERNS);
        userAttributesField.setFieldValue(DEFAULT_USER_ATTRIBUTES);
        groupSearchBaseField.setFieldValue(DEFAULT_GROUP_SEARCH_BASE);
        groupSearchFilterField.setFieldValue(DEFAULT_GROUP_SEARCH_FILTER);
        groupRoleAttributeField.setFieldValue(DEFAULT_GROUP_ROLE_ATTRIBUTE);
        rolePrefixField.setFieldValue(DEFAULT_ROLE_PREFIX);

        configurationModel.put(enabledField);
        configurationModel.put(serverField);
        configurationModel.put(managerDNField);
        configurationModel.put(managerPasswordField);
        configurationModel.put(authenticationTypeField);
        configurationModel.put(referralField);
        configurationModel.put(userSearchBaseField);
        configurationModel.put(userSearchFilterField);
        configurationModel.put(userDNPatternsField);
        configurationModel.put(userAttributesField);
        configurationModel.put(groupSearchBaseField);
        configurationModel.put(groupSearchFilterField);
        configurationModel.put(groupRoleAttributeField);
        configurationModel.put(rolePrefixField);

        return configurationModel;
    }

    @Test
    public void testUpdate() throws Exception {
        final ConfigurationModel configurationModel = createConfigurationModel();
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);

        final ConfigurationModel updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(DEFAULT_ENABLED, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_ENABLED).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_SERVER, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_SERVER).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_MANAGER_DN, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_MANAGER_DN).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_MANAGER_PASSWORD, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_MANAGER_PWD).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_AUTHENTICATION_TYPE, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_REFERRAL, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_REFERRAL).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_USER_SEARCH_BASE, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_USER_SEARCH_FILTER, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_USER_DN_PATTERNS, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_USER_ATTRIBUTES, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_USER_ATTRIBUTES).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_GROUP_SEARCH_BASE, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_BASE).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_GROUP_SEARCH_FILTER, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_GROUP_ROLE_ATTRIBUTE, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE).flatMap(field -> field.getFieldValue()).orElse(null));
        assertEquals(DEFAULT_ROLE_PREFIX, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_ROLE_PREFIX).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testIsEnabled() throws Exception {
        final ConfigurationModel configurationModel = createConfigurationModel();
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        assertTrue(ldapManager.isLdapEnabled());
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_ENABLED).ifPresent(field -> field.setFieldValue("false"));
        assertFalse(ldapManager.isLdapEnabled());
    }

    @Test
    public void testAuthenticationTypeSimple() throws Exception {
        final String authenticationType = "simple";
        final ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        ldapManager.updateContext();
        final ConfigurationModel updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(authenticationType, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testAuthenticationTypeDigest() throws Exception {
        final String authenticationType = "digest";
        final ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        ldapManager.updateContext();
        final ConfigurationModel updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(authenticationType, updatedProperties.getField(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testAuthenticationProviderCreated() throws Exception {
        final ConfigurationModel configurationModel = createConfigurationModel();
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        assertNotNull(ldapManager.getAuthenticationProvider());
    }

    @Test
    public void testExceptionOnContext() throws Exception {
        final String managerDN = "";
        final String managerPassword = "";

        final ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_SERVER).get().setFieldValue(null);
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_MANAGER_DN).get().setFieldValue(managerDN);
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_MANAGER_PWD).get().setFieldValue(managerPassword);
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        try {
            ldapManager.updateContext();
            fail();
        } catch (final AlertLDAPConfigurationException ex) {
            // exception occurred
        }
    }

    @Test
    public void testExceptionOnAuthenticator() throws Exception {

        final String userSearchBase = "";
        final String userSearchFilter = "";
        final String userDNPatterns = "";

        final ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE).get().setFieldValue(userSearchBase);
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER).get().setFieldValue(userSearchFilter);
        configurationModel.getField(SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS).get().setFieldValue(userDNPatterns);
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(Mockito.anyString())).thenReturn(List.of(configurationModel));
        final LdapManager ldapManager = new LdapManager(configurationAccessor);
        try {
            ldapManager.updateContext();
            fail();
        } catch (final AlertLDAPConfigurationException ex) {
            // exception occurred
        }
    }
}
