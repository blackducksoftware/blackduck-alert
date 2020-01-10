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
package com.synopsys.integration.alert.workflow.startup.component;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLContext;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;

@Component
@Order(8)
public class SAMLStartupComponent extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(SAMLStartupComponent.class);
    private final SAMLContext samlContext;
    private final SAMLManager samlManager;

    @Autowired
    public SAMLStartupComponent(SAMLContext samlContext, SAMLManager samlManager) {
        this.samlContext = samlContext;
        this.samlManager = samlManager;
    }

    @Override
    protected void initialize() {
        try {
            ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            boolean samlEnabled = samlContext.isSAMLEnabled(currentConfiguration);
            String metadataURL = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_METADATA_URL);
            String entityId = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_ENTITY_ID);
            String entityBaseUrl = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL);
            if (samlEnabled) {
                samlManager.setupMetadataManager(metadataURL, entityId, entityBaseUrl);
            }
        } catch (AlertConfigurationException e) {
            logger.warn(String.format("Cannot start the SAML identity provider. %s", e.getMessage()));
        } catch (AlertException | MetadataProviderException e) {
            logger.error("Error adding the SAML identity provider.", e);
        }
    }
}
