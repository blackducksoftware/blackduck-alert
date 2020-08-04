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
package com.synopsys.integration.alert.component.authentication.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationUIConfig;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AuthenticationTestAction extends TestAction {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationTestAction.class);
    private final LdapManager ldapManager;
    private final SAMLManager samlManager;

    @Autowired
    public AuthenticationTestAction(LdapManager ldapManager, SAMLManager samlManager) {
        this.ldapManager = ldapManager;
        this.samlManager = samlManager;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        logger.info("Testing authentication.");
        boolean ldapEnabled = registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_LDAP_ENABLED);
        boolean samlEnabled = registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_SAML_ENABLED);
        if (!ldapEnabled && !samlEnabled) {
            String errorMessage = "Enable LDAP or SAML authentication.";
            List<AlertFieldStatus> errorsMap = List.of(
                AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage),
                AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENABLED, errorMessage));
            throw new AlertFieldException(errorsMap);
        }

        if (ldapEnabled) {
            performLdapTest(fieldModel, registeredFieldValues);
        }

        if (samlEnabled) {
            performSAMLTest(registeredFieldValues);
        }

        return new MessageResult("Successfully tested authentication configuration.");
    }

    private void performLdapTest(FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        logger.info("LDAP enabled testing LDAP authentication.");
        String userName = fieldModel.getFieldValue(AuthenticationUIConfig.TEST_FIELD_KEY_USERNAME).orElse("");
        Optional<LdapAuthenticationProvider> ldapProvider = ldapManager.createAuthProvider(registeredFieldValues);
        String errorMessage = String.format("Ldap Authentication test failed for the test user %s.  Please check the LDAP configuration.", userName);
        List<AlertFieldStatus> errorsMap = new ArrayList<>();
        if (!ldapProvider.isPresent()) {
            errorsMap.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
        } else {
            Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(userName,
                fieldModel.getFieldValue(AuthenticationUIConfig.TEST_FIELD_KEY_PASSWORD).orElse(""));
            Authentication authentication = ldapProvider.get().authenticate(pendingAuthentication);
            if (!authentication.isAuthenticated()) {
                errorsMap.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
            }
            authentication.setAuthenticated(false);
        }

        if (!errorsMap.isEmpty()) {
            throw new AlertFieldException(errorsMap);
        }
    }

    private void performSAMLTest(FieldAccessor registeredFieldValues) throws IntegrationException {
        Optional<ConfigurationFieldModel> metaDataURLField = registeredFieldValues.getField(AuthenticationDescriptor.KEY_SAML_METADATA_URL);
        Optional<ConfigurationFieldModel> metaDataFileField = registeredFieldValues.getField(AuthenticationDescriptor.KEY_SAML_METADATA_FILE);
        boolean testMetaDataURL = metaDataURLField.map(ConfigurationFieldModel::isSet).orElse(false);
        boolean testMetaDataFile = metaDataFileField.map(ConfigurationFieldModel::isSet).orElse(false);
        List<AlertFieldStatus> errorsMap = new ArrayList<>();
        if (testMetaDataURL) {
            logger.info("Testing SAML Metadata URL...");
            try {
                Optional<ExtendedMetadataDelegate> provider = samlManager.createHttpProvider(registeredFieldValues.getStringOrEmpty(AuthenticationDescriptor.KEY_SAML_METADATA_URL));
                if (provider.isPresent()) {
                    provider.get().initialize();
                }
            } catch (Exception ex) {
                logger.error("Testing SAML Metadata URL error: ", ex);
                errorsMap.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_URL, ex.getMessage()));
            }
        }

        if (testMetaDataFile) {
            logger.info("Testing SAML Metadata File...");
            try {
                Optional<ExtendedMetadataDelegate> provider = samlManager.createFileProvider();
                if (provider.isPresent()) {
                    provider.get().initialize();
                }
            } catch (Exception ex) {
                logger.error("Testing SAML Metadata File error: ", ex);
                errorsMap.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_FILE, ex.getMessage()));
            }
        }
        samlManager.initializeConfiguration();
        if (!errorsMap.isEmpty()) {
            throw new AlertFieldException(errorsMap);
        }
    }
}
