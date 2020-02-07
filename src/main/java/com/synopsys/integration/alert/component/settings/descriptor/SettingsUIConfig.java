/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.component.settings.descriptor;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class SettingsUIConfig extends UIConfig {
    private static final String LABEL_DEFAULT_SYSTEM_ADMINISTRATOR_EMAIL = "Default System Administrator Email";
    private static final String LABEL_DEFAULT_SYSTEM_ADMINISTRATOR_PASSWORD = "Default System Administrator Password";
    private static final String LABEL_ENCRYPTION_PASSWORD = "Encryption Password";
    private static final String LABEL_ENCRYPTION_GLOBAL_SALT = "Encryption Global Salt";
    private static final String LABEL_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE = "Startup Environment Variable Override";
    private static final String LABEL_PROXY_HOST = "Proxy Host";
    private static final String LABEL_PROXY_PORT = "Proxy Port";
    private static final String LABEL_PROXY_USERNAME = "Proxy Username";
    private static final String LABEL_PROXY_PASSWORD = "Proxy Password";
    private static final String LABEL_LDAP_ENABLED = "LDAP Enabled";
    private static final String LABEL_LDAP_SERVER = "LDAP Server";
    private static final String LABEL_LDAP_MANAGER_DN = "LDAP Manager DN";
    private static final String LABEL_LDAP_MANAGER_PASSWORD = "LDAP Manager Password";
    private static final String LABEL_LDAP_AUTHENTICATION_TYPE = "LDAP Authentication Type";
    private static final String LABEL_LDAP_REFERRAL = "LDAP Referral";
    private static final String LABEL_LDAP_USER_SEARCH_BASE = "LDAP User Search Base";
    private static final String LABEL_LDAP_USER_SEARCH_FILTER = "LDAP User Search Filter";
    private static final String LABEL_LDAP_USER_DN_PATTERNS = "LDAP User DN Patterns";
    private static final String LABEL_LDAP_USER_ATTRIBUTES = "LDAP User Attributes";
    private static final String LABEL_LDAP_GROUP_SEARCH_BASE = "LDAP Group Search Base";
    private static final String LABEL_LDAP_GROUP_SEARCH_FILTER = "LDAP Group Search Filter";
    private static final String LABEL_LDAP_GROUP_ROLE_ATTRIBUTE = "LDAP Group Role Attribute";
    private static final String LABEL_SAML_ENABLED = "SAML Enabled";
    private static final String LABEL_SAML_FORCE_AUTH = "Force Auth";
    private static final String LABEL_SAML_METADATA_URL = "Identity Provider Metadata URL";
    private static final String LABEL_SAML_ENTITY_ID = "Entity ID";
    private static final String LABEL_SAML_ENTITY_BASE_URL = "Entity Base URL";

    private static final String SETTINGS_ADMIN_EMAIL_DESCRIPTION = "The email address of the Alert system administrator. Used in case a password reset is needed.";
    private static final String SETTINGS_USER_PASSWORD_DESCRIPTION = "The password of the Alert system administrator. Used when logging in as the \"sysadmin\" user.";
    private static final String SETTINGS_ENCRYPTION_PASSWORD_DESCRIPTION = "The password used when encrypting sensitive fields. Must be at least 8 characters long.";
    private static final String SETTINGS_ENCRYPTION_SALT_DESCRIPTION = "The salt used when encrypting sensitive fields. Must be at least 8 characters long.";
    private static final String SETTINGS_ENVIRONMENT_VARIABLE_OVERRIDE_DESCRIPTION = "If true, the Alert environment variables will override the stored configurations.";
    private static final String SETTINGS_PROXY_HOST_DESCRIPTION = "The host name of the proxy server to use.";
    private static final String SETTINGS_PROXY_PORT_DESCRIPTION = "The port of the proxy server to use.";
    private static final String SETTINGS_PROXY_USERNAME_DESCRIPTION = "If the proxy server requires authentication, the username to authenticate with the proxy server.";
    private static final String SETTINGS_PROXY_PASSWORD_DESCRIPTION = "If the proxy server requires authentication, the password to authenticate with the proxy server.";
    private static final String SETTINGS_LDAP_ENABLED_DESCRIPTION = "If true, Alert with attempt to authenticate using the LDAP configuration.";
    private static final String SETTINGS_LDAP_SERVER_DESCRIPTION = "The URL of the LDAP server.";
    private static final String SETTINGS_LDAP_MANAGER_DN_DESCRIPTION = "The distinguished name of the LDAP manager.";
    private static final String SETTINGS_LDAP_MANAGER_PASSWORD_DESCRIPTION = "The password of the LDAP manager.";
    private static final String SETTINGS_LDAP_AUTHENTICATION_TYPE_DESCRIPTION = "The type of authentication required to connect to the LDAP server.";
    private static final String SETTINGS_LDAP_REFERRAL_DESCRIPTION = "Set the method to handle referrals.";
    private static final String SETTINGS_LDAP_USER_SEARCH_BASE_DESCRIPTION = "The part of the LDAP directory in which user searches should be done.";
    private static final String SETTINGS_LDAP_USER_SEARCH_FILTER_DESCRIPTION = "The filter used to search for user membership.";
    private static final String SETTINGS_LDAP_USER_DN_PATTERNS_DESCRIPTION = "The pattern used used to supply a DN for the user. The pattern should be the name relative to the root DN.";
    private static final String SETTINGS_LDAP_USER_ATTRIBUTES_DESCRIPTION = "User attributes to retrieve for users.";
    private static final String SETTINGS_LDAP_GROUP_SEARCH_BASE_DESCRIPTION = "The part of the LDAP directory in which group searches should be done.";
    private static final String SETTINGS_LDAP_GROUP_SEARCH_FILTER_DESCRIPTION = "The filter used to search for group membership.";
    private static final String SETTINGS_LDAP_GROUP_ROLE_ATTRIBUTE_DESCRIPTION = "The ID of the attribute which contains the role name for a group.";
    private static final String SETTINGS_SAML_ENABLED_DESCRIPTION = "If true, Alert will attempt to authenticate using the SAML configuration.";
    private static final String SETTINGS_SAML_FORCE_AUTH_DESCRIPTION = "If true, the forceAuthn flag is set to true in the SAML request to the IDP. Please check the IDP if this is supported.";
    private static final String SETTINGS_SAML_METADATA_URL_DESCRIPTION = "The Metadata URL from the external Identity Provider.";
    private static final String SETTINGS_SAML_ENTITY_ID_DESCRIPTION = "The Entity ID of the Service Provider. EX: This should be the Audience defined in Okta.";
    private static final String SETTINGS_SAML_ENTITY_BASE_URL_DESCRIPTION = "This should be the URL of the Alert system.";

    private static final String SETTINGS_PANEL_PROXY = "Proxy Configuration";
    private static final String SETTINGS_PANEL_LDAP = "LDAP Configuration";
    private static final String SETTINGS_PANEL_SAML = "SAML Configuration";

    private static final String SETTINGS_HEADER_ADMINISTRATOR = "Default System Administrator Configuration";
    private static final String SETTINGS_HEADER_ENCRYPTION = "Encryption Configuration";

    public SettingsUIConfig() {
        super(SettingsDescriptor.SETTINGS_LABEL, SettingsDescriptor.SETTINGS_DESCRIPTION, SettingsDescriptor.SETTINGS_URL, SettingsDescriptor.SETTINGS_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        // Startup settings
        final ConfigField sysAdminEmail = TextInputConfigField.createRequired(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, LABEL_DEFAULT_SYSTEM_ADMINISTRATOR_EMAIL, SETTINGS_ADMIN_EMAIL_DESCRIPTION)
                                              .setHeader(SETTINGS_HEADER_ADMINISTRATOR);
        final ConfigField defaultUserPassword = PasswordConfigField.createRequired(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, LABEL_DEFAULT_SYSTEM_ADMINISTRATOR_PASSWORD, SETTINGS_USER_PASSWORD_DESCRIPTION)
                                                    .setHeader(SETTINGS_HEADER_ADMINISTRATOR);
        final ConfigField encryptionPassword = PasswordConfigField.createRequired(SettingsDescriptor.KEY_ENCRYPTION_PWD, LABEL_ENCRYPTION_PASSWORD, SETTINGS_ENCRYPTION_PASSWORD_DESCRIPTION, this::minimumEncryptionFieldLength)
                                                   .setHeader(SETTINGS_HEADER_ENCRYPTION);
        final ConfigField encryptionSalt = PasswordConfigField.createRequired(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, LABEL_ENCRYPTION_GLOBAL_SALT, SETTINGS_ENCRYPTION_SALT_DESCRIPTION, this::minimumEncryptionFieldLength)
                                               .setHeader(SETTINGS_HEADER_ENCRYPTION);
        final ConfigField environmentVariableOverride = CheckboxConfigField
                                                            .create(SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE, LABEL_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE, SETTINGS_ENVIRONMENT_VARIABLE_OVERRIDE_DESCRIPTION);

        // Proxy settings
        final ConfigField proxyHost = TextInputConfigField.create(SettingsDescriptor.KEY_PROXY_HOST, LABEL_PROXY_HOST, SETTINGS_PROXY_HOST_DESCRIPTION);
        final ConfigField proxyPort = NumberConfigField.create(SettingsDescriptor.KEY_PROXY_PORT, LABEL_PROXY_PORT, SETTINGS_PROXY_PORT_DESCRIPTION);
        final ConfigField proxyUsername = TextInputConfigField.create(SettingsDescriptor.KEY_PROXY_USERNAME, LABEL_PROXY_USERNAME, SETTINGS_PROXY_USERNAME_DESCRIPTION);
        final ConfigField proxyPassword = PasswordConfigField.create(SettingsDescriptor.KEY_PROXY_PWD, LABEL_PROXY_PASSWORD, SETTINGS_PROXY_PASSWORD_DESCRIPTION);
        proxyHost
            .setPanel(SETTINGS_PANEL_PROXY)
            .requireField(proxyPort.getKey());
        proxyPort
            .setPanel(SETTINGS_PANEL_PROXY)
            .requireField(proxyHost.getKey());
        proxyUsername
            .setPanel(SETTINGS_PANEL_PROXY)
            .requireField(proxyHost.getKey())
            .requireField(proxyPassword.getKey());
        proxyPassword
            .setPanel(SETTINGS_PANEL_PROXY)
            .requireField(proxyHost.getKey())
            .requireField(proxyUsername.getKey());

        // Ldap settings
        final ConfigField ldapServer = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_SERVER, LABEL_LDAP_SERVER, SETTINGS_LDAP_SERVER_DESCRIPTION)
                                           .setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapManagerDn = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_MANAGER_DN, LABEL_LDAP_MANAGER_DN, SETTINGS_LDAP_MANAGER_DN_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapManagerPassword = PasswordConfigField.create(SettingsDescriptor.KEY_LDAP_MANAGER_PWD, LABEL_LDAP_MANAGER_PASSWORD, SETTINGS_LDAP_MANAGER_PASSWORD_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapAuthenticationType = SelectConfigField
                                                       .create(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE, LABEL_LDAP_AUTHENTICATION_TYPE, SETTINGS_LDAP_AUTHENTICATION_TYPE_DESCRIPTION, List.of(
                                                           new LabelValueSelectOption("Simple", "simple"),
                                                           new LabelValueSelectOption("None", "none"),
                                                           new LabelValueSelectOption("Digest-MD5", "digest")))
                                                       .setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapReferral = SelectConfigField.create(SettingsDescriptor.KEY_LDAP_REFERRAL, LABEL_LDAP_REFERRAL, SETTINGS_LDAP_REFERRAL_DESCRIPTION, List.of(
            new LabelValueSelectOption("Ignore", "ignore"),
            new LabelValueSelectOption("Follow", "follow"),
            new LabelValueSelectOption("Throw", "throw")))
                                             .setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapUserSearchBase = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE, LABEL_LDAP_USER_SEARCH_BASE, SETTINGS_LDAP_USER_SEARCH_BASE_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapUserSearchFilter = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER, LABEL_LDAP_USER_SEARCH_FILTER, SETTINGS_LDAP_USER_SEARCH_FILTER_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapUserDNPatterns = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS, LABEL_LDAP_USER_DN_PATTERNS, SETTINGS_LDAP_USER_DN_PATTERNS_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapUserAttributes = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_ATTRIBUTES, LABEL_LDAP_USER_ATTRIBUTES, SETTINGS_LDAP_USER_ATTRIBUTES_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapGroupSearchBase = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_BASE, LABEL_LDAP_GROUP_SEARCH_BASE, SETTINGS_LDAP_GROUP_SEARCH_BASE_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapGroupSearchFilter = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER, LABEL_LDAP_GROUP_SEARCH_FILTER, SETTINGS_LDAP_GROUP_SEARCH_FILTER_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapGroupRoleAttribute = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE, LABEL_LDAP_GROUP_ROLE_ATTRIBUTE, SETTINGS_LDAP_GROUP_ROLE_ATTRIBUTE_DESCRIPTION).setPanel(SETTINGS_PANEL_LDAP);
        final ConfigField ldapEnabled = CheckboxConfigField.create(SettingsDescriptor.KEY_LDAP_ENABLED, LABEL_LDAP_ENABLED, SETTINGS_LDAP_ENABLED_DESCRIPTION)
                                            .requireField(ldapServer.getKey())
                                            .requireField(ldapManagerDn.getKey())
                                            .requireField(ldapManagerPassword.getKey())
                                            .disallowField(SettingsDescriptor.KEY_SAML_ENABLED)
                                            .setPanel(SETTINGS_PANEL_LDAP);

        // Saml settings
        final ConfigField samlForceAuth = CheckboxConfigField.create(SettingsDescriptor.KEY_SAML_FORCE_AUTH, LABEL_SAML_FORCE_AUTH, SETTINGS_SAML_FORCE_AUTH_DESCRIPTION).setPanel(SETTINGS_PANEL_SAML);
        final ConfigField samlMetaDataURL = TextInputConfigField.create(SettingsDescriptor.KEY_SAML_METADATA_URL, LABEL_SAML_METADATA_URL, SETTINGS_SAML_METADATA_URL_DESCRIPTION).setPanel(SETTINGS_PANEL_SAML);
        final ConfigField samlEntityId = TextInputConfigField.create(SettingsDescriptor.KEY_SAML_ENTITY_ID, LABEL_SAML_ENTITY_ID, SETTINGS_SAML_ENTITY_ID_DESCRIPTION).setPanel(SETTINGS_PANEL_SAML);
        final ConfigField samlEntityBaseURL = TextInputConfigField.create(SettingsDescriptor.KEY_SAML_ENTITY_BASE_URL, LABEL_SAML_ENTITY_BASE_URL, SETTINGS_SAML_ENTITY_BASE_URL_DESCRIPTION).setPanel(SETTINGS_PANEL_SAML);
        final ConfigField samlEnabled = CheckboxConfigField.create(SettingsDescriptor.KEY_SAML_ENABLED, LABEL_SAML_ENABLED, SETTINGS_SAML_ENABLED_DESCRIPTION)
                                            .requireField(samlForceAuth.getKey())
                                            .requireField(samlMetaDataURL.getKey())
                                            .requireField(samlEntityId.getKey())
                                            .requireField(samlEntityBaseURL.getKey())
                                            .disallowField(SettingsDescriptor.KEY_LDAP_ENABLED)
                                            .setPanel(SETTINGS_PANEL_SAML);

        return List.of(sysAdminEmail, defaultUserPassword, encryptionPassword, encryptionSalt, environmentVariableOverride, proxyHost, proxyPort, proxyUsername, proxyPassword, ldapEnabled, ldapServer, ldapManagerDn, ldapManagerPassword,
            ldapAuthenticationType, ldapReferral, ldapUserSearchBase, ldapUserSearchFilter, ldapUserDNPatterns, ldapUserAttributes, ldapGroupSearchBase, ldapGroupSearchFilter, ldapGroupRoleAttribute, samlEnabled, samlForceAuth,
            samlMetaDataURL,
            samlEntityId, samlEntityBaseURL);
    }

    private Collection<String> minimumEncryptionFieldLength(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        if (fieldToValidate.hasValues() && fieldToValidate.getValue().orElse("").length() < 8) {
            return List.of(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT);
        }
        return List.of();
    }

}
