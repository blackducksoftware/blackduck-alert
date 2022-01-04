/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.EntityDescriptor;
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
import org.springframework.security.saml.metadata.MetadataMemoryProvider;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

public class SAMLManager {
    public static final Logger logger = LoggerFactory.getLogger(SAMLManager.class);
    private final ParserPool parserPool;
    private final ExtendedMetadata extendedMetadata;
    private final MetadataManager metadataManager;
    private final MetadataGenerator metadataGenerator;
    private final FilePersistenceUtil filePersistenceUtil;
    private final SAMLContext samlContext;

    public SAMLManager(ParserPool parserPool, ExtendedMetadata extendedMetadata, MetadataManager metadataManager, MetadataGenerator metadataGenerator,
        FilePersistenceUtil filePersistenceUtil, SAMLContext samlContext) {
        this.parserPool = parserPool;
        this.extendedMetadata = extendedMetadata;
        this.metadataManager = metadataManager;
        this.metadataGenerator = metadataGenerator;
        this.filePersistenceUtil = filePersistenceUtil;
        this.samlContext = samlContext;
    }

    public void initializeConfiguration() {
        logger.info("Initializing SAML identity provider with database configuration.");
        try {
            ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            boolean samlEnabled = samlContext.isSAMLEnabled(currentConfiguration);
            String metadataURL = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_METADATA_URL);
            String entityId = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_ENTITY_ID);
            String entityBaseUrl = samlContext.getFieldValueOrEmpty(currentConfiguration, AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL);

            if (samlEnabled) {
                // validate the metadata providers.  If they are still valid enable SAML.
                samlEnabled = checkMetadataProvidersValid(metadataURL, entityId);
            }
            updateSAMLConfiguration(samlEnabled, metadataURL, entityId, entityBaseUrl);
        } catch (AlertConfigurationException e) {
            logger.warn(String.format("Cannot initialize the SAML identity provider. %s", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error initializing the SAML identity provider.", e);
        }
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
            } else {
                samlContext.disableSAML();
            }
        } catch (Exception e) {
            logger.error("Error updating the SAML identity provider.", e);
        }
    }

    public void setupMetadataManager(String metadataURL, String entityId, String entityBaseUrl) throws MetadataProviderException {
        logger.debug("SAML Setup MetaData Manager");
        logger.debug("SAML - MetadataURL: {}, EntityID: {}, EntityBaseURL: {}", metadataURL, entityId, entityBaseUrl);
        metadataGenerator.setEntityId(entityId);
        metadataGenerator.setEntityBaseURL(entityBaseUrl);

        Optional<ExtendedMetadataDelegate> httpProvider = createHttpProvider(metadataURL);
        Optional<ExtendedMetadataDelegate> fileProvider = createFileProvider();
        Optional<ExtendedMetadataDelegate> memoryProvider = createMemoryProvider();
        List<MetadataProvider> providers = List.of(httpProvider, fileProvider, memoryProvider).stream()
                                               .flatMap(Optional::stream)
                                               .collect(Collectors.toList());
        metadataManager.setProviders(providers);
        metadataManager.setHostedSPName(entityId);
        metadataManager.afterPropertiesSet();
    }

    public Optional<ExtendedMetadataDelegate> createHttpProvider(String metadataUrl) throws MetadataProviderException {
        if (StringUtils.isBlank(metadataUrl)) {
            return Optional.empty();
        }
        logger.debug("SAML - Create Http Metadata provider.");
        // The URL can not end in a '/' because it messes with the paths for saml
        String correctedMetadataURL = StringUtils.removeEnd(metadataUrl, "/");
        Timer backgroundTaskTimer = new Timer(true);
        HTTPMetadataProvider provider = new HTTPMetadataProvider(backgroundTaskTimer, httpClient(), correctedMetadataURL);
        provider.setParserPool(parserPool);
        return Optional.of(createDelegate(provider));
    }

    public Optional<ExtendedMetadataDelegate> createFileProvider() throws MetadataProviderException {
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

    // This needs to be created in order for Azure AD SAML configuration to work. The entity id in the metadata is different
    // than the entity id configured in Azure.  This allows the the entity id to get mapped and found correctly for the application.
    private Optional<ExtendedMetadataDelegate> createMemoryProvider() throws MetadataProviderException {
        EntityDescriptor descriptor = metadataGenerator.generateMetadata();
        MetadataMemoryProvider provider = new MetadataMemoryProvider(descriptor);
        provider.initialize();
        return Optional.of(createDelegate(provider));
    }

    private ExtendedMetadataDelegate createDelegate(MetadataProvider provider) {
        ExtendedMetadataDelegate delegate = new ExtendedMetadataDelegate(provider, extendedMetadata);
        delegate.setMetadataTrustCheck(false);
        delegate.setMetadataRequireSignature(false);
        return delegate;
    }

    private HttpClient httpClient() {
        return new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    private boolean checkMetadataProvidersValid(String metaDataUrl, String entityId) {
        boolean httpProviderValid = false;
        boolean fileProviderValid = false;
        boolean entityIdValid = StringUtils.isNotBlank(entityId);
        if (!entityIdValid) {
            logger.error("Validated SAML entity id missing.");
        }
        try {
            Optional<ExtendedMetadataDelegate> provider = createHttpProvider(metaDataUrl);
            if (provider.isPresent()) {
                provider.get().initialize();
                httpProviderValid = true;
            }
        } catch (Exception ex) {
            logger.error("Validating SAML Metadata URL error: ", ex);
        }

        try {
            Optional<ExtendedMetadataDelegate> provider = createFileProvider();
            if (provider.isPresent()) {
                provider.get().initialize();
                fileProviderValid = true;
            }
        } catch (Exception ex) {
            logger.error("Validating SAML Metadata File error: ", ex);
        }
        return entityIdValid && (httpProviderValid || fileProviderValid);
    }
}
