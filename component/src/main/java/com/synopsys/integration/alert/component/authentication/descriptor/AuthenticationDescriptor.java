/**
 * component
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
package com.synopsys.integration.alert.component.authentication.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;

@Component
public class AuthenticationDescriptor extends ComponentDescriptor {
    public static final String AUTHENTICATION_LABEL = "Authentication";
    public static final String AUTHENTICATION_URL = "authentication";
    public static final String AUTHENTICATION_DESCRIPTION = "This page allows you to configure user authentication for Alert.";

    // LDAP Keys
    public static final String KEY_LDAP_ENABLED = "settings.ldap.enabled";
    public static final String KEY_LDAP_SERVER = "settings.ldap.server";
    public static final String KEY_LDAP_MANAGER_DN = "settings.ldap.manager.dn";
    public static final String KEY_LDAP_MANAGER_PWD = "settings.ldap.manager.password";
    public static final String KEY_LDAP_AUTHENTICATION_TYPE = "settings.ldap.authentication.type";
    public static final String KEY_LDAP_REFERRAL = "settings.ldap.referral";
    public static final String KEY_LDAP_USER_SEARCH_BASE = "settings.ldap.user.search.base";
    public static final String KEY_LDAP_USER_SEARCH_FILTER = "settings.ldap.user.search.filter";
    public static final String KEY_LDAP_USER_DN_PATTERNS = "settings.ldap.user.dn.patterns";
    public static final String KEY_LDAP_USER_ATTRIBUTES = "settings.ldap.user.attributes";
    public static final String KEY_LDAP_GROUP_SEARCH_BASE = "settings.ldap.group.search.base";
    public static final String KEY_LDAP_GROUP_SEARCH_FILTER = "settings.ldap.group.search.filter";
    public static final String KEY_LDAP_GROUP_ROLE_ATTRIBUTE = "settings.ldap.group.role.attribute";

    // SAML Keys
    public static final String KEY_SAML_ENABLED = "settings.saml.enabled";
    public static final String KEY_SAML_FORCE_AUTH = "settings.saml.force.auth";
    public static final String KEY_SAML_METADATA_URL = "settings.saml.metadata.url";
    public static final String KEY_SAML_ENTITY_ID = "settings.saml.entity.id";
    public static final String KEY_SAML_ENTITY_BASE_URL = "settings.saml.entity.base.url";
    public static final String KEY_SAML_METADATA_FILE = "settings.saml.metadata.file";

    // User Management
    // Role Mappings
    public static final String KEY_ROLE_MAPPING_NAME_ADMIN = "settings.role.mapping.name.admin";
    public static final String KEY_ROLE_MAPPING_NAME_JOB_MANAGER = "settings.role.mapping.name.job.manager";
    public static final String KEY_ROLE_MAPPING_NAME_USER = "settings.role.mapping.name.user";
    // SAML Role Attribute Mapping
    public static final String KEY_SAML_ROLE_ATTRIBUTE_MAPPING = "settings.saml.role.attribute.mapping.name";

    public static final String FIELD_ERROR_LDAP_SERVER_MISSING = "LDAP Server is missing";

    public static final String FIELD_ERROR_SAML_METADATA_URL_MISSING = "SAML Metadata URL is missing and a Metadata file has not been uploaded.";
    public static final String FIELD_ERROR_SAML_METADATA_FILE_MISSING = "SAML Metadata file has not been uploaded and a Metadata URL has not been specified.";

    public static final String SAML_METADATA_FILE = "saml_metadata.xml";

    @Autowired
    public AuthenticationDescriptor(AuthenticationDescriptorKey descriptorKey, AuthenticationUIConfig componentUIConfig) {
        super(descriptorKey, componentUIConfig);
    }
}
