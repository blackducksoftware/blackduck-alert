package com.synopsys.integration.alert.web.security.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;

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
    private static final AuthenticationDescriptorKey AUTHENTICATION_DESCRIPTOR_KEY = new AuthenticationDescriptorKey();

    private ConfigurationModel createConfigurationModel() {
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, null, null, ConfigContextEnum.GLOBAL);

        ConfigurationFieldModel enabledField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_ENABLED);
        ConfigurationFieldModel serverField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_SERVER);
        ConfigurationFieldModel managerDNField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN);
        ConfigurationFieldModel managerPasswordField = ConfigurationFieldModel.createSensitive(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD);
        ConfigurationFieldModel authenticationTypeField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE);
        ConfigurationFieldModel referralField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_REFERRAL);
        ConfigurationFieldModel userSearchBaseField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        ConfigurationFieldModel userSearchFilterField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        ConfigurationFieldModel userDNPatternsField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS);
        ConfigurationFieldModel userAttributesField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_USER_ATTRIBUTES);
        ConfigurationFieldModel groupSearchBaseField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_BASE);
        ConfigurationFieldModel groupSearchFilterField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER);
        ConfigurationFieldModel groupRoleAttributeField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE);

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
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));

        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);

        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        Assertions.assertEquals(DEFAULT_ENABLED, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_ENABLED).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_SERVER, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_SERVER).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_MANAGER_DN, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_MANAGER_PASSWORD, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_AUTHENTICATION_TYPE, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_REFERRAL, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_REFERRAL).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_USER_SEARCH_BASE, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_USER_SEARCH_FILTER, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_USER_DN_PATTERNS, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_USER_ATTRIBUTES, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_USER_ATTRIBUTES).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_GROUP_SEARCH_BASE, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_BASE).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_GROUP_SEARCH_FILTER, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER).flatMap(field -> field.getFieldValue()).orElse(null));
        Assertions.assertEquals(DEFAULT_GROUP_ROLE_ATTRIBUTE, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testIsEnabled() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        Assertions.assertTrue(ldapManager.isLdapEnabled());
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_ENABLED).ifPresent(field -> field.setFieldValue("false"));
        Assertions.assertFalse(ldapManager.isLdapEnabled());
    }

    @Test
    public void testAuthenticationTypeSimple() throws Exception {
        final String authenticationType = "simple";
        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        ldapManager.getAuthenticationProvider();
        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        Assertions.assertEquals(authenticationType, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testAuthenticationTypeDigest() throws Exception {
        final String authenticationType = "digest";
        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).get().setFieldValue(authenticationType);
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        ldapManager.getAuthenticationProvider();
        FieldUtility updatedProperties = ldapManager.getCurrentConfiguration();
        Assertions.assertEquals(authenticationType, updatedProperties.getField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE).flatMap(field -> field.getFieldValue()).orElse(null));
    }

    @Test
    public void testAuthenticationProviderCreated() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        assertNotNull(ldapManager.getAuthenticationProvider());
    }

    @Test
    public void testExceptionOnContext() throws Exception {
        final String managerDN = "";
        final String managerPassword = "";

        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_SERVER).get().setFieldValue(null);
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN).get().setFieldValue(managerDN);
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD).get().setFieldValue(managerPassword);
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        try {
            ldapManager.getAuthenticationProvider();
            fail();
        } catch (AlertConfigurationException ex) {
            // exception occurred
        }
    }

    @Test
    public void testExceptionOnAuthenticator() throws Exception {

        final String userSearchBase = "";
        final String userSearchFilter = "";
        final String userDNPatterns = "";

        ConfigurationModel configurationModel = createConfigurationModel();
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE).get().setFieldValue(userSearchBase);
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER).get().setFieldValue(userSearchFilter);
        configurationModel.getField(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS).get().setFieldValue(userDNPatterns);
        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        LdapManager ldapManager = new LdapManager(AUTHENTICATION_DESCRIPTOR_KEY, configurationAccessor, authoritiesPopulator);
        try {
            ldapManager.getAuthenticationProvider();
            fail();
        } catch (AlertConfigurationException ex) {
            // exception occurred
        }
    }

}
