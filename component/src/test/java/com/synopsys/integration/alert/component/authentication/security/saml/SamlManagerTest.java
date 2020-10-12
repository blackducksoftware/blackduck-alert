package com.synopsys.integration.alert.component.authentication.security.saml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.security.saml.key.EmptyKeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataManager;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

public class SamlManagerTest {

    @Test
    public void testUpdateSamlDisabled() throws Exception {
        Gson gson = new Gson();
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = new CachingMetadataManager(Collections.emptyList());
        metadataManager.setKeyManager(new EmptyKeyManager());
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        ConfigurationModel currentConfiguration = Mockito.mock(ConfigurationModel.class);
        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertSecretsDir()).thenReturn("./testDB/run/secrets");
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
        Mockito.when(context.getCurrentConfiguration()).thenReturn(currentConfiguration);
        Mockito.when(context.isSAMLEnabled(Mockito.any(ConfigurationModel.class))).thenReturn(Boolean.FALSE.booleanValue());

        SAMLManager samlManager = new SAMLManager(parserPool, extendedMetadata, metadataManager, metadataGenerator, filePersistenceUtil, context);
        samlManager.updateSAMLConfiguration(Boolean.FALSE.booleanValue(), "metadataURL", "entityId", "baseURL");
        Mockito.verify(metadataGenerator).setEntityId(null);
        Mockito.verify(metadataGenerator).setEntityBaseURL(null);
        assertTrue(metadataManager.getAvailableProviders().isEmpty());
        assertNull(metadataManager.getHostedSPName());
    }

    @Test
    public void testUpdateSamlEnabled() throws Exception {
        Gson gson = new Gson();
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = new CachingMetadataManager(Collections.emptyList());
        metadataManager.setKeyManager(new EmptyKeyManager());
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        ConfigurationModel currentConfiguration = Mockito.mock(ConfigurationModel.class);
        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertSecretsDir()).thenReturn("data/test/secrets");
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
        Mockito.when(context.getCurrentConfiguration()).thenReturn(currentConfiguration);
        Mockito.when(context.isSAMLEnabled(Mockito.any(ConfigurationModel.class))).thenReturn(Boolean.TRUE.booleanValue());
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.anyString())).thenReturn("metadataURL");
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.anyString())).thenReturn("entityId");
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.anyString())).thenReturn("baseURL");

        SAMLManager samlManager = new SAMLManager(parserPool, extendedMetadata, metadataManager, metadataGenerator, filePersistenceUtil, context);
        samlManager.updateSAMLConfiguration(Boolean.TRUE.booleanValue(), "metadataURL", "entityId", "baseURL");

        Mockito.verify(metadataGenerator).setEntityId(Mockito.anyString());
        Mockito.verify(metadataGenerator).setEntityBaseURL(Mockito.anyString());
        // these methods are called to clear the existing metadata and then set it if true.
        assertFalse(metadataManager.getAvailableProviders().isEmpty());
        assertNotNull(metadataManager.getHostedSPName());
    }
}
