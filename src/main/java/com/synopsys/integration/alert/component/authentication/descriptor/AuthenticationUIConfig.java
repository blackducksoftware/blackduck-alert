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
package com.synopsys.integration.alert.component.authentication.descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.UploadFileButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class AuthenticationUIConfig extends UIConfig {
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
    private static final String LABEL_SAML_METADATA_FILE = "Identity Provider Metadata File";
    private static final String LABEL_SAML_METADATA_FILE_UPLOAD = "Upload";
    private static final String LABEL_USER_MANAGEMENT_ROLE_MAPPING_ADMIN = "Admin User Role Name";
    private static final String LABEL_USER_MANAGEMENT_ROLE_MAPPING_JOB_MANAGER = "Job Manager Role Name";
    private static final String LABEL_USER_MANAGEMENT_ROLE_MAPPING_USER = "User Role Name";
    private static final String LABEL_USER_MANAGEMENT_SAML_ATTRIBUTE_MAPPING = "SAML Role Attribute Mapping";

    private static final String AUTHENTICATION_LDAP_ENABLED_DESCRIPTION = "If true, Alert with attempt to authenticate using the LDAP configuration.";
    private static final String AUTHENTICATION_LDAP_SERVER_DESCRIPTION = "The URL of the LDAP server.";
    private static final String AUTHENTICATION_LDAP_MANAGER_DN_DESCRIPTION = "The distinguished name of the LDAP manager.";
    private static final String AUTHENTICATION_LDAP_MANAGER_PASSWORD_DESCRIPTION = "The password of the LDAP manager.";
    private static final String AUTHENTICATION_LDAP_AUTHENTICATION_TYPE_DESCRIPTION = "The type of authentication required to connect to the LDAP server.";
    private static final String AUTHENTICATION_LDAP_REFERRAL_DESCRIPTION = "Set the method to handle referrals.";
    private static final String AUTHENTICATION_LDAP_USER_SEARCH_BASE_DESCRIPTION = "The part of the LDAP directory in which user searches should be done.";
    private static final String AUTHENTICATION_LDAP_USER_SEARCH_FILTER_DESCRIPTION = "The filter used to search for user membership.";
    private static final String AUTHENTICATION_LDAP_USER_DN_PATTERNS_DESCRIPTION = "The pattern used used to supply a DN for the user. The pattern should be the name relative to the root DN.";
    private static final String AUTHENTICATION_LDAP_USER_ATTRIBUTES_DESCRIPTION = "User attributes to retrieve for users.";
    private static final String AUTHENTICATION_LDAP_GROUP_SEARCH_BASE_DESCRIPTION = "The part of the LDAP directory in which group searches should be done.";
    private static final String AUTHENTICATION_LDAP_GROUP_SEARCH_FILTER_DESCRIPTION = "The filter used to search for group membership.";
    private static final String AUTHENTICATION_LDAP_GROUP_ROLE_ATTRIBUTE_DESCRIPTION = "The ID of the attribute which contains the role name for a group.";
    private static final String AUTHENTICATION_SAML_ENABLED_DESCRIPTION = "If true, Alert will attempt to authenticate using the SAML configuration.";
    private static final String AUTHENTICATION_SAML_FORCE_AUTH_DESCRIPTION = "If true, the forceAuthn flag is set to true in the SAML request to the IDP. Please check the IDP if this is supported.";
    private static final String AUTHENTICATION_SAML_METADATA_URL_DESCRIPTION = "The Metadata URL from the external Identity Provider.";
    private static final String AUTHENTICATION_SAML_ENTITY_ID_DESCRIPTION = "The Entity ID of the Service Provider. EX: This should be the Audience defined in Okta.";
    private static final String AUTHENTICATION_SAML_ENTITY_BASE_URL_DESCRIPTION = "This should be the URL of the Alert system.";
    private static final String AUTHENTICATION_SAML_METADATA_FILE_DESCRIPTION = "The file to upload to the server containing the Metadata from the external Identity Provider.";

    private static final String AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_ADMIN_DESCRIPTION = "The role name to map to the Admin role of Alert.";
    private static final String AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_JOB_MANAGER_DESCRIPTION = "The role name to map to the Job Manager role of Alert.";
    private static final String AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_USER_DESCRIPTION = "The role name to map to a User role of Alert.";
    private static final String AUTHENTICATION_USER_MANAGEMENT_SAML_ATTRIBUTE_MAPPING_DESCRIPTION = "The SAML attribute in the Attribute Statements that contains the roles for the user logged into Alert.  The roles contained in the Attribute Statement can be the role names defined in the mapping fields above.";

    private static final String AUTHENTICATION_HEADER_LDAP = "LDAP Configuration";
    private static final String AUTHENTICATION_HEADER_SAML = "SAML Configuration";
    private static final String AUTHENTICATION_PANEL_USER_MANAGEMENT = "User Management";
    private static final String AUTHENTICATION_HEADER_ROLE_MAPPING = "User Role Mapping";
    private static final String AUTHENTICATION_HEADER_USER_MANAGEMENT_SAML = "SAML";

    private final EncryptionSettingsValidator encryptionValidator;
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public AuthenticationUIConfig(FilePersistenceUtil filePersistenceUtil, EncryptionSettingsValidator encryptionValidator) {
        super(AuthenticationDescriptor.AUTHENTICATION_LABEL, AuthenticationDescriptor.AUTHENTICATION_DESCRIPTION, AuthenticationDescriptor.AUTHENTICATION_URL);
        this.filePersistenceUtil = filePersistenceUtil;
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        List<ConfigField> ldapPanelFields = createLDAPPanel();
        ldapPanelFields.stream().forEach(field -> field.applyPanel(AUTHENTICATION_HEADER_LDAP).applyHeader(AUTHENTICATION_HEADER_LDAP));
        List<ConfigField> samlPanelFields = createSAMLPanel();
        samlPanelFields.stream().forEach(field -> field.applyPanel(AUTHENTICATION_HEADER_SAML).applyHeader(AUTHENTICATION_HEADER_SAML));
        List<ConfigField> userManagement = createUserManagementPanel();
        userManagement.stream().forEach(field -> field.applyPanel(AUTHENTICATION_PANEL_USER_MANAGEMENT));

        List<List<ConfigField>> fieldLists = List.of(ldapPanelFields, samlPanelFields, userManagement);
        return fieldLists.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<ConfigField> createUserManagementPanel() {
        ConfigField adminRoleMapping = new TextInputConfigField(AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_ADMIN, LABEL_USER_MANAGEMENT_ROLE_MAPPING_ADMIN, AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_ADMIN_DESCRIPTION)
                                           .applyHeader(AUTHENTICATION_HEADER_ROLE_MAPPING);
        ConfigField jobManagerRoleMapping = new TextInputConfigField(AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_JOB_MANAGER, LABEL_USER_MANAGEMENT_ROLE_MAPPING_JOB_MANAGER,
            AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_JOB_MANAGER_DESCRIPTION)
                                                .applyHeader(AUTHENTICATION_HEADER_ROLE_MAPPING);
        ConfigField userRoleMapping = new TextInputConfigField(AuthenticationDescriptor.KEY_ROLE_MAPPING_NAME_USER, LABEL_USER_MANAGEMENT_ROLE_MAPPING_USER, AUTHENTICATION_USER_MANAGEMENT_ROLE_MAPPING_USER_DESCRIPTION)
                                          .applyHeader(AUTHENTICATION_HEADER_ROLE_MAPPING);
        ConfigField samlAttributeMapping = new TextInputConfigField(AuthenticationDescriptor.KEY_SAML_ROLE_ATTRIBUTE_MAPPING, LABEL_USER_MANAGEMENT_SAML_ATTRIBUTE_MAPPING, AUTHENTICATION_USER_MANAGEMENT_SAML_ATTRIBUTE_MAPPING_DESCRIPTION)
                                               .applyHeader(AUTHENTICATION_HEADER_USER_MANAGEMENT_SAML);

        return List.of(adminRoleMapping, jobManagerRoleMapping, userRoleMapping, samlAttributeMapping);
    }

    private List<ConfigField> createLDAPPanel() {
        ConfigField ldapServer = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_SERVER, LABEL_LDAP_SERVER, AUTHENTICATION_LDAP_SERVER_DESCRIPTION).applyHeader(AUTHENTICATION_HEADER_LDAP);
        ConfigField ldapManagerDn = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN, LABEL_LDAP_MANAGER_DN, AUTHENTICATION_LDAP_MANAGER_DN_DESCRIPTION).applyHeader(AUTHENTICATION_HEADER_LDAP);
        ConfigField ldapManagerPassword = new PasswordConfigField(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD, LABEL_LDAP_MANAGER_PASSWORD, AUTHENTICATION_LDAP_MANAGER_PASSWORD_DESCRIPTION, encryptionValidator);
        ConfigField ldapAuthenticationType = new SelectConfigField(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE, LABEL_LDAP_AUTHENTICATION_TYPE, AUTHENTICATION_LDAP_AUTHENTICATION_TYPE_DESCRIPTION, List.of(
            new LabelValueSelectOption("Simple", "simple"),
            new LabelValueSelectOption("None", "none"),
            new LabelValueSelectOption("Digest-MD5", "digest"))
        );
        ConfigField ldapReferral = new SelectConfigField(AuthenticationDescriptor.KEY_LDAP_REFERRAL, LABEL_LDAP_REFERRAL, AUTHENTICATION_LDAP_REFERRAL_DESCRIPTION, List.of(
            new LabelValueSelectOption("Ignore", "ignore"),
            new LabelValueSelectOption("Follow", "follow"),
            new LabelValueSelectOption("Throw", "throw"))
        );
        ConfigField ldapUserSearchBase = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE, LABEL_LDAP_USER_SEARCH_BASE, AUTHENTICATION_LDAP_USER_SEARCH_BASE_DESCRIPTION);
        ConfigField ldapUserSearchFilter = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER, LABEL_LDAP_USER_SEARCH_FILTER, AUTHENTICATION_LDAP_USER_SEARCH_FILTER_DESCRIPTION);
        ConfigField ldapUserDNPatterns = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS, LABEL_LDAP_USER_DN_PATTERNS, AUTHENTICATION_LDAP_USER_DN_PATTERNS_DESCRIPTION);
        ConfigField ldapUserAttributes = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_USER_ATTRIBUTES, LABEL_LDAP_USER_ATTRIBUTES, AUTHENTICATION_LDAP_USER_ATTRIBUTES_DESCRIPTION);
        ConfigField ldapGroupSearchBase = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_BASE, LABEL_LDAP_GROUP_SEARCH_BASE, AUTHENTICATION_LDAP_GROUP_SEARCH_BASE_DESCRIPTION);
        ConfigField ldapGroupSearchFilter = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER, LABEL_LDAP_GROUP_SEARCH_FILTER, AUTHENTICATION_LDAP_GROUP_SEARCH_FILTER_DESCRIPTION);
        ConfigField ldapGroupRoleAttribute = new TextInputConfigField(AuthenticationDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE, LABEL_LDAP_GROUP_ROLE_ATTRIBUTE, AUTHENTICATION_LDAP_GROUP_ROLE_ATTRIBUTE_DESCRIPTION);
        ConfigField ldapEnabled = new CheckboxConfigField(AuthenticationDescriptor.KEY_LDAP_ENABLED, LABEL_LDAP_ENABLED, AUTHENTICATION_LDAP_ENABLED_DESCRIPTION)
                                      .applyRequiredRelatedField(ldapServer.getKey())
                                      .applyRequiredRelatedField(ldapManagerDn.getKey())
                                      .applyRequiredRelatedField(ldapManagerPassword.getKey())
                                      .applyDisallowedRelatedField(AuthenticationDescriptor.KEY_SAML_ENABLED);
        return List.of(ldapEnabled, ldapServer, ldapManagerDn, ldapManagerPassword, ldapAuthenticationType, ldapReferral, ldapUserSearchBase, ldapUserSearchFilter, ldapUserDNPatterns, ldapUserAttributes, ldapGroupSearchBase,
            ldapGroupSearchFilter, ldapGroupRoleAttribute);
    }

    private List<ConfigField> createSAMLPanel() {
        ConfigField samlForceAuth = new CheckboxConfigField(AuthenticationDescriptor.KEY_SAML_FORCE_AUTH, LABEL_SAML_FORCE_AUTH, AUTHENTICATION_SAML_FORCE_AUTH_DESCRIPTION).applyHeader(AUTHENTICATION_HEADER_SAML);
        ConfigField samlMetaDataURL = new TextInputConfigField(AuthenticationDescriptor.KEY_SAML_METADATA_URL, LABEL_SAML_METADATA_URL, AUTHENTICATION_SAML_METADATA_URL_DESCRIPTION)
                                          .applyValidationFunctions(this::validateMetaDataUrl);
        ConfigField samlMetaDataFile = new UploadFileButtonField(AuthenticationDescriptor.KEY_SAML_METADATA_FILE, LABEL_SAML_METADATA_FILE, AUTHENTICATION_SAML_METADATA_FILE_DESCRIPTION, LABEL_SAML_METADATA_FILE_UPLOAD, List.of(
            "text/xml",
            "application/xml",
            ".xml"),
            "", false)
                                           .applyValidationFunctions(this::validateMetaDataFile);
        ConfigField samlEntityId = new TextInputConfigField(AuthenticationDescriptor.KEY_SAML_ENTITY_ID, LABEL_SAML_ENTITY_ID, AUTHENTICATION_SAML_ENTITY_ID_DESCRIPTION);
        ConfigField samlEntityBaseURL = new TextInputConfigField(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL, LABEL_SAML_ENTITY_BASE_URL, AUTHENTICATION_SAML_ENTITY_BASE_URL_DESCRIPTION)
                                            .applyValidationFunctions(this::validateMetaDataUrl);
        ConfigField samlEnabled = new CheckboxConfigField(AuthenticationDescriptor.KEY_SAML_ENABLED, LABEL_SAML_ENABLED, AUTHENTICATION_SAML_ENABLED_DESCRIPTION)
                                      .applyRequiredRelatedField(samlForceAuth.getKey())
                                      .applyRequiredRelatedField(samlEntityId.getKey())
                                      .applyRequiredRelatedField(samlEntityBaseURL.getKey())
                                      .applyDisallowedRelatedField(AuthenticationDescriptor.KEY_LDAP_ENABLED);
        return List.of(samlEnabled, samlForceAuth, samlMetaDataURL, samlMetaDataFile, samlEntityId, samlEntityBaseURL);
    }

    private Collection<String> validateMetaDataUrl(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Optional<FieldValueModel> samlEnabledField = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_ENABLED);
        boolean samlEnabled = samlEnabledField.flatMap(FieldValueModel::getValue)
                                  .map(Boolean::valueOf)
                                  .orElse(false);
        if (samlEnabled) {
            if (!fieldToValidate.hasValues() && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
                return List.of(AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING);
            }
        }
        return List.of();
    }

    private Collection<String> validateMetaDataFile(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Optional<FieldValueModel> samlEnabledField = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_ENABLED);
        boolean samlEnabled = samlEnabledField.flatMap(FieldValueModel::getValue)
                                  .map(Boolean::valueOf)
                                  .orElse(false);
        if (samlEnabled) {
            Optional<FieldValueModel> metadataUrlField = fieldModel.getFieldValueModel(AuthenticationDescriptor.KEY_SAML_METADATA_URL);
            boolean metadataUrlEmpty = metadataUrlField.map(field -> !field.hasValues()).orElse(true);
            if (metadataUrlEmpty && !filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
                return List.of(AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING);
            }
        }
        return List.of();
    }

}
