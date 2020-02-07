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
package com.synopsys.integration.alert.web.security.authentication.saml;

import java.util.List;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataManager;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

public class SAMLManager {
    public static final Logger logger = LoggerFactory.getLogger(SAMLManager.class);
    private final SAMLContext samlContext;
    private final ParserPool parserPool;
    private final ExtendedMetadata extendedMetadata;
    private final MetadataManager metadataManager;
    private final MetadataGenerator metadataGenerator;

    public SAMLManager(final SAMLContext samlContext, final ParserPool parserPool, final ExtendedMetadata extendedMetadata, final MetadataManager metadataManager, final MetadataGenerator metadataGenerator) {
        this.samlContext = samlContext;
        this.parserPool = parserPool;
        this.extendedMetadata = extendedMetadata;
        this.metadataManager = metadataManager;
        this.metadataGenerator = metadataGenerator;
    }

    public void initializeSAML() {
        try {
            final ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            final boolean samlEnabled = samlContext.isSAMLEnabled(currentConfiguration);
            final String metadataURL = samlContext.getFieldValueOrEmpty(currentConfiguration, SettingsDescriptor.KEY_SAML_METADATA_URL);
            final String entityId = samlContext.getFieldValueOrEmpty(currentConfiguration, SettingsDescriptor.KEY_SAML_ENTITY_ID);
            final String entityBaseUrl = samlContext.getFieldValueOrEmpty(currentConfiguration, SettingsDescriptor.KEY_SAML_ENTITY_BASE_URL);
            if (samlEnabled) {
                setupMetadataManager(metadataURL, entityId, entityBaseUrl);
            }
        } catch (final AlertDatabaseConstraintException | AlertLDAPConfigurationException | MetadataProviderException e) {
            logger.error("Error adding the SAML identity provider.", e);
        }
    }

    public void updateSAMLConfiguration(final boolean samlEnabled, final String metadataURL, final String entityId, final String entityBaseUrl) {
        try {
            if (samlEnabled) {
                if (StringUtils.isNotBlank(metadataURL)) {
                    setupMetadataManager(metadataURL, entityId, entityBaseUrl);
                }
            } else {
                final List<ExtendedMetadataDelegate> currentProviders = metadataManager.getAvailableProviders();
                currentProviders.forEach(ExtendedMetadataDelegate::destroy);
                metadataManager.setProviders(List.of());
                metadataManager.setDefaultIDP(null);
                metadataManager.setHostedSPName(null);
                metadataManager.afterPropertiesSet();
                metadataGenerator.setEntityId(null);
                metadataGenerator.setEntityBaseURL(null);
            }
        } catch (final MetadataProviderException e) {
            logger.error("Error updating the SAML identity provider.", e);
        }
    }

    private void setupMetadataManager(final String metadataURL, final String entityId, final String entityBaseUrl) throws MetadataProviderException {
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setEntityBaseURL(entityBaseUrl);

        final Timer backgroundTaskTimer = new Timer(true);

        // The URL can not end in a '/' because it messes with the paths for saml
        final String correctedMetadataURL = StringUtils.removeEnd(metadataURL, "/");
        final HTTPMetadataProvider httpMetadataProvider;
        httpMetadataProvider = new HTTPMetadataProvider(backgroundTaskTimer, new HttpClient(), correctedMetadataURL);
        httpMetadataProvider.setParserPool(parserPool);

        final ExtendedMetadataDelegate idpMetadata = new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata);
        idpMetadata.setMetadataTrustCheck(true);
        idpMetadata.setMetadataRequireSignature(false);
        metadataManager.setProviders(List.of(idpMetadata));
        metadataManager.afterPropertiesSet();
    }

}
