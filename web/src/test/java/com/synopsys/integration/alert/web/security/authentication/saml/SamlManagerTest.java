package com.synopsys.integration.alert.web.security.authentication.saml;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataManager;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

public class SamlManagerTest {

    @Test
    public void testUpdateSamlDisabled() throws Exception {
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = Mockito.mock(MetadataManager.class);
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        ConfigurationModel currentConfiguration = Mockito.mock(ConfigurationModel.class);
        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        Mockito.when(context.getCurrentConfiguration()).thenReturn(currentConfiguration);
        Mockito.when(context.isSAMLEnabled(Mockito.any(ConfigurationModel.class))).thenReturn(Boolean.FALSE.booleanValue());

        SAMLManager samlManager = new SAMLManager(parserPool, extendedMetadata, metadataManager, metadataGenerator, filePersistenceUtil, context);
        samlManager.updateSAMLConfiguration(Boolean.FALSE.booleanValue(), "metadataURL", "entityId", "baseURL");
        Mockito.verify(metadataGenerator).setEntityId(null);
        Mockito.verify(metadataGenerator).setEntityBaseURL(null);
        Mockito.verify(metadataManager).setProviders(Mockito.anyList());
        Mockito.verify(metadataManager).setDefaultIDP(null);
        Mockito.verify(metadataManager).setHostedSPName(null);
        Mockito.verify(metadataManager).afterPropertiesSet();
    }

    @Test
    public void testUpdateSamlEnabled() throws Exception {
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = Mockito.mock(MetadataManager.class);
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        ConfigurationModel currentConfiguration = Mockito.mock(ConfigurationModel.class);
        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
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
        Mockito.verify(metadataManager, Mockito.times(2)).setProviders(Mockito.anyList());
        Mockito.verify(metadataManager, Mockito.times(2)).afterPropertiesSet();
    }
}
