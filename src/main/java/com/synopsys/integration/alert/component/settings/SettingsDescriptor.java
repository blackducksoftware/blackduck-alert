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
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;

@Component
public class SettingsDescriptor extends ComponentDescriptor {
    public static final String SETTINGS_COMPONENT = "component_settings";
    public static final String SETTINGS_LABEL = "Settings";
    public static final String SETTINGS_URL = "settings";
    public static final String SETTINGS_ICON = "cog";
    // KEYS not stored in the database
    public static final String KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD = "user.default.admin.password";
    public static final String KEY_ENCRYPTION_PASSWORD = "encryption.password";
    public static final String KEY_ENCRYPTION_GLOBAL_SALT = "encryption.global.salt";

    // Proxy Keys
    public static final String KEY_PROXY_HOST = "proxy.host";
    public static final String KEY_PROXY_PORT = "proxy.port";
    public static final String KEY_PROXY_USERNAME = "proxy.username";
    public static final String KEY_PROXY_PASSWORD = "proxy.password";

    // LDAP Keys
    public static final String KEY_LDAP_ENABLED = "ldap.enabled";
    public static final String KEY_LDAP_SERVER = "ldap.server";
    public static final String KEY_LDAP_MANAGER_DN = "ldap.manager.dn";
    public static final String KEY_LDAP_MANAGER_PASSWORD = "ldap.manager.password";
    public static final String KEY_LDAP_AUTHENTICATION_TYPE = "ldap.authentication.type";
    public static final String KEY_LDAP_REFERRAL = "ldap.referral";
    public static final String KEY_LDAP_USER_SEARCH_BASE = "ldap.user.search.base";
    public static final String KEY_LDAP_USER_SEARCH_FILTER = "ldap.user.search.filter";
    public static final String KEY_LDAP_USER_DN_PATTERNS = "ldap.user.dn.patterns";
    public static final String KEY_LDAP_USER_ATTRIBUTES = "ldap.user.attributes";
    public static final String KEY_LDAP_GROUP_SEARCH_BASE = "ldap.group.search.base";
    public static final String KEY_LDAP_GROUP_SEARCH_FILTER = "ldap.group.search.filter";
    public static final String KEY_LDAP_GROUP_ROLE_ATTRIBUTE = "ldap.group.role.attribute";
    public static final String KEY_LDAP_ROLE_PREFIX = "ldap.role.prefix";

    public static final String FIELD_ERROR_DEFAULT_USER_PASSWORD = "Default admin user password missing";
    public static final String FIELD_ERROR_ENCRYPTION_PASSWORD = "Encryption password missing";
    public static final String FIELD_ERROR_ENCRYPTION_GLOBAL_SALT = "Encryption global salt missing";

    @Autowired
    public SettingsDescriptor(final SettingsDescriptorActionApi componentRestApi, final SettingsUIConfig uiConfig) {
        super(SETTINGS_COMPONENT, componentRestApi, uiConfig);
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.GLOBAL == context) {
            final Collection<DefinedFieldModel> fields = new LinkedList<>();
            fields.add(DefinedFieldModel.createGlobalField(KEY_PROXY_HOST));
            fields.add(DefinedFieldModel.createGlobalField(KEY_PROXY_PORT));
            fields.add(DefinedFieldModel.createGlobalField(KEY_PROXY_USERNAME));
            fields.add(DefinedFieldModel.createGlobalSensitiveField(KEY_PROXY_PASSWORD));

            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_ENABLED));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_SERVER));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_MANAGER_DN));
            fields.add(DefinedFieldModel.createGlobalSensitiveField(KEY_LDAP_MANAGER_PASSWORD));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_AUTHENTICATION_TYPE));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_REFERRAL));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_USER_SEARCH_BASE));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_USER_SEARCH_FILTER));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_USER_DN_PATTERNS));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_USER_ATTRIBUTES));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_GROUP_SEARCH_BASE));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_GROUP_SEARCH_FILTER));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_GROUP_ROLE_ATTRIBUTE));
            fields.add(DefinedFieldModel.createGlobalField(KEY_LDAP_ROLE_PREFIX));
            return fields;
        }

        return List.of();
    }
}
