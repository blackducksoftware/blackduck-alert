package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.ldap.LDAPAuthoritiesPopulator;
import com.synopsys.integration.alert.authentication.ldap.descriptor.LDAPDescriptor;
import com.synopsys.integration.alert.authentication.ldap.descriptor.LDAPDescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.database.api.DefaultConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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

    private static final LDAPDescriptorKey AUTHENTICATION_DESCRIPTOR_KEY = new LDAPDescriptorKey();
    private static final InetOrgPersonContextMapper LDAP_USER_CONTEXT_MAPPER = new InetOrgPersonContextMapper();

    private ConfigurationModel createConfigurationModel() {
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, null, null, ConfigContextEnum.GLOBAL);

        ConfigurationFieldModel enabledField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_ENABLED);
        ConfigurationFieldModel serverField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_SERVER);
        ConfigurationFieldModel managerDNField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_MANAGER_DN);
        ConfigurationFieldModel managerPasswordField = ConfigurationFieldModel.createSensitive(LDAPDescriptor.KEY_LDAP_MANAGER_PWD);
        ConfigurationFieldModel authenticationTypeField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE);
        ConfigurationFieldModel referralField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_REFERRAL);
        ConfigurationFieldModel userSearchBaseField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        ConfigurationFieldModel userSearchFilterField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        ConfigurationFieldModel userDNPatternsField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_USER_DN_PATTERNS);
        ConfigurationFieldModel userAttributesField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_USER_ATTRIBUTES);
        ConfigurationFieldModel groupSearchBaseField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_GROUP_SEARCH_BASE);
        ConfigurationFieldModel groupSearchFilterField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER);
        ConfigurationFieldModel groupRoleAttributeField = ConfigurationFieldModel.create(LDAPDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE);

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

        return configurationModel;
    }

    @Test
    public void testUpdate() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));

        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);

        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(DEFAULT_ENABLED, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_ENABLED).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_SERVER, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_SERVER).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_MANAGER_DN, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_MANAGER_DN).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_MANAGER_PASSWORD, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_MANAGER_PWD).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(
            DEFAULT_AUTHENTICATION_TYPE,
            updatedProperties.getField(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null)
        );
        assertEquals(DEFAULT_REFERRAL, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_REFERRAL).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_USER_SEARCH_BASE, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_USER_SEARCH_BASE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(
            DEFAULT_USER_SEARCH_FILTER,
            updatedProperties.getField(LDAPDescriptor.KEY_LDAP_USER_SEARCH_FILTER).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null)
        );
        assertEquals(DEFAULT_USER_DN_PATTERNS, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_USER_DN_PATTERNS).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_USER_ATTRIBUTES, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_USER_ATTRIBUTES).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(DEFAULT_GROUP_SEARCH_BASE, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_GROUP_SEARCH_BASE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
        assertEquals(
            DEFAULT_GROUP_SEARCH_FILTER,
            updatedProperties.getField(LDAPDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null)
        );
        assertEquals(
            DEFAULT_GROUP_ROLE_ATTRIBUTE,
            updatedProperties.getField(LDAPDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null)
        );
    }

    @Test
    public void testIsEnabled() {
        ConfigurationModel configurationModel = createConfigurationModel();
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        assertTrue(ldapManager.isLdapEnabled());
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_ENABLED).ifPresent(field -> field.setFieldValue("false"));
        assertFalse(ldapManager.isLdapEnabled());
    }

    @Test
    public void testAuthenticationTypeSimple() throws Exception {
        final String authenticationType = "simple";
        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        ldapManager.getAuthenticationProvider();
        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(authenticationType, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
    }

    @Test
    public void testAuthenticationTypeDigest() throws Exception {
        final String authenticationType = "digest";
        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        ldapManager.getAuthenticationProvider();
        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(authenticationType, updatedProperties.getField(LDAPDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(ConfigurationFieldModel::getFieldValue).orElse(null));
    }

    @Test
    public void testAuthenticationProviderCreated() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        assertNotNull(ldapManager.getAuthenticationProvider());
    }

    @Test
    public void testExceptionOnContext() {
        final String managerDN = "";
        final String managerPassword = "";

        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_SERVER).get().setFieldValue(null);
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_MANAGER_DN).get().setFieldValue(managerDN);
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_MANAGER_PWD).get().setFieldValue(managerPassword);
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        try {
            ldapManager.getAuthenticationProvider();
            fail();
        } catch (AlertConfigurationException ex) {
            // exception occurred
        }
    }

    @Test
    public void testExceptionOnAuthenticator() {
        final String userSearchBase = "";
        final String userSearchFilter = "";
        final String userDNPatterns = "";

        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_USER_SEARCH_BASE).get().setFieldValue(userSearchBase);
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_USER_SEARCH_FILTER).get().setFieldValue(userSearchFilter);
        configurationModel.getField(LDAPDescriptor.KEY_LDAP_USER_DN_PATTERNS).get().setFieldValue(userDNPatterns);
        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LDAPAuthoritiesPopulator ldapAuthoritiesPopulator = Mockito.mock(LDAPAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationModelConfigurationAccessor, ldapAuthoritiesPopulator, LDAP_USER_CONTEXT_MAPPER);
        try {
            ldapManager.getAuthenticationProvider();
            fail();
        } catch (AlertConfigurationException ex) {
            // exception occurred
        }
    }

}