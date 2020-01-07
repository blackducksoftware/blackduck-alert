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

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataManager;

import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

public class SAMLManager {
    public static final Logger logger = LoggerFactory.getLogger(SAMLManager.class);
    private final ParserPool parserPool;
    private final ExtendedMetadata extendedMetadata;
    private final MetadataManager metadataManager;
    private final MetadataGenerator metadataGenerator;
    private final FilePersistenceUtil filePersistenceUtil;

    public SAMLManager(ParserPool parserPool, ExtendedMetadata extendedMetadata, MetadataManager metadataManager, MetadataGenerator metadataGenerator,
        FilePersistenceUtil filePersistenceUtil) {
        this.parserPool = parserPool;
        this.extendedMetadata = extendedMetadata;
        this.metadataManager = metadataManager;
        this.metadataGenerator = metadataGenerator;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public void updateSAMLConfiguration(boolean samlEnabled, String metadataURL, String entityId, String entityBaseUrl) {
        try {
            logger.debug("SAML Config Update.");
            List<ExtendedMetadataDelegate> currentProviders = metadataManager.getAvailableProviders();
            currentProviders.forEach(ExtendedMetadataDelegate::destroy);
            metadataManager.setProviders(List.of());
            metadataManager.setDefaultIDP(null);
            metadataManager.setHostedSPName(null);
            metadataManager.afterPropertiesSet();
            metadataGenerator.setEntityId(null);
            metadataGenerator.setEntityBaseURL(null);
            logger.debug("SAML cleared configuration.");
            if (samlEnabled) {
                setupMetadataManager(metadataURL, entityId, entityBaseUrl);
            }
        } catch (MetadataProviderException e) {
            logger.error("Error updating the SAML identity provider.", e);
        }
    }

    public void setupMetadataManager(String metadataURL, String entityId, String entityBaseUrl) throws MetadataProviderException {
        logger.debug("SAML Setup MetaData Manager");
        logger.debug("SAML - MetadataUrl: {}, EntityID: {}, EntityBaseUrl: {}", metadataURL, entityId, entityBaseUrl);
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setEntityBaseURL(entityBaseUrl);

        Optional<MetadataProvider> httpProvider = createHttpProvider(metadataURL);
        Optional<MetadataProvider> fileProvider = createFileProvider();
        List<MetadataProvider> providers = List.of(httpProvider, fileProvider).stream()
                                               .flatMap(Optional::stream)
                                               .collect(Collectors.toList());
        metadataManager.setProviders(providers);
        metadataManager.afterPropertiesSet();
    }

    private Optional<MetadataProvider> createHttpProvider(String metadataUrl) throws MetadataProviderException {
        if (StringUtils.isBlank(metadataUrl)) {
            return Optional.empty();
        }
        logger.debug("SAML - Create Http Metadata provider.");
        // The URL can not end in a '/' because it messes with the paths for saml
        String correctedMetadataURL = StringUtils.removeEnd(metadataUrl, "/");
        Timer backgroundTaskTimer = new Timer(true);
        HTTPMetadataProvider provider = new HTTPMetadataProvider(backgroundTaskTimer, new HttpClient(), correctedMetadataURL);
        provider.setParserPool(parserPool);
        return Optional.of(createDelegate(provider));
    }

    private Optional<MetadataProvider> createFileProvider() throws MetadataProviderException {
        Timer backgroundTaskTimer = new Timer(true);
        if (!filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)) {
            return Optional.empty();
        }
        logger.debug("SAML - Create File Metadata provider.");
        File metadataFile = filePersistenceUtil.createUploadsFile(AuthenticationDescriptor.SAML_METADATA_FILE);
        FilesystemMetadataProvider provider = new FilesystemMetadataProvider(backgroundTaskTimer, metadataFile);
        provider.setParserPool(parserPool);
        return Optional.of(createDelegate(provider));
    }

    private ExtendedMetadataDelegate createDelegate(MetadataProvider provider) {
        ExtendedMetadataDelegate delegate = new ExtendedMetadataDelegate(provider, extendedMetadata);
        delegate.setMetadataTrustCheck(true);
        delegate.setMetadataRequireSignature(false);
        return delegate;
    }
}
