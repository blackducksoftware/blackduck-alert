/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.component.settings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

@Component
public class SettingsUIConfig extends UIConfig {
    public SettingsUIConfig() {
        super(SettingsDescriptor.SETTINGS_LABEL, SettingsDescriptor.SETTINGS_URL, SettingsDescriptor.SETTINGS_ICON);
    }

    @Override
    public List<ConfigField> createFields() {

        final ConfigField defaultUserPassword = PasswordConfigField.createRequired(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, "Default System Adminstrator Password");
        final ConfigField encryptionPassword = PasswordConfigField.createRequired(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, "Encryption Password");
        final ConfigField encryptionSalt = PasswordConfigField.createRequired(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, "Encryption Global Salt");

        final ConfigField proxyHost = TextInputConfigField.create(SettingsDescriptor.KEY_PROXY_HOST, "Proxy Host");
        final ConfigField proxyPort = TextInputConfigField.create(SettingsDescriptor.KEY_PROXY_PORT, "Proxy Port");
        final ConfigField proxyUsername = TextInputConfigField.create(SettingsDescriptor.KEY_PROXY_USERNAME, "Proxy Username");
        final ConfigField proxyPassword = PasswordConfigField.create(SettingsDescriptor.KEY_PROXY_PASSWORD, "Proxy Password");

        final ConfigField ldapEnabled = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_ENABLED, "LDAP Enabled");
        final ConfigField ldapServer = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_SERVER, "LDAP Server", this::validateLDAPServer);
        final ConfigField ldapManagerDn = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_MANAGER_DN, "LDAP Manager DN");
        final ConfigField ldapManagerPassword = PasswordConfigField.create(SettingsDescriptor.KEY_LDAP_MANAGER_PASSWORD, "LDAP Manager Password");
        final ConfigField ldapAuthenticationType = SelectConfigField.create(SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE, "LDAP Authentication Type", List.of("simple", "none", "digest"));
        final ConfigField ldapReferral = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_REFERRAL, "LDAP Referral");
        final ConfigField ldapUserSearchBase = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE, "LDAP User Search Base");
        final ConfigField ldapUserSearchFilter = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER, "LDAP User Search Filter");
        final ConfigField ldapUserDNPatterns = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS, "LDAP User DN Patterns");
        final ConfigField ldapUserAttributes = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_USER_ATTRIBUTES, "LDAP User Attributes");
        final ConfigField ldapGroupSearchBase = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_BASE, "LDAP Group Search Base");
        final ConfigField ldapGroupSearchFilter = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER, "LDAP Group Search Filter");
        final ConfigField ldapGroupRoleAttribute = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE, "LDAP Group Role Attribute");
        final ConfigField ldapRolePrefix = TextInputConfigField.create(SettingsDescriptor.KEY_LDAP_ROLE_PREFIX, "LDAP Role Prefix");

        return List.of(defaultUserPassword, encryptionPassword, encryptionSalt, proxyHost, proxyPort, proxyUsername, proxyPassword, ldapEnabled, ldapServer, ldapManagerDn, ldapManagerPassword, ldapAuthenticationType, ldapReferral,
            ldapUserSearchBase, ldapUserSearchFilter, ldapUserDNPatterns, ldapUserAttributes, ldapGroupSearchBase, ldapGroupSearchFilter, ldapGroupRoleAttribute, ldapRolePrefix);
    }

    private Collection<String> validateLDAPServer(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final Optional<FieldValueModel> ldapEnabled = fieldModel.getField(SettingsDescriptor.KEY_LDAP_ENABLED);
        if (ldapEnabled.isPresent()) {
            final Boolean isLdapEnabled = Boolean.valueOf(ldapEnabled.get().getValue().orElse("false"));
            if (isLdapEnabled) {
                if (!fieldToValidate.hasValues() || StringUtils.isBlank(fieldToValidate.getValue().orElse(""))) {
                    return List.of(SettingsDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING);
                }
            }
        }
        return List.of();
    }
}
