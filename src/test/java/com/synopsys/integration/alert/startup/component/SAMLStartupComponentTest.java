package com.synopsys.integration.alert.startup.component;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataManager;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.security.saml.SAMLContext;
import com.synopsys.integration.alert.component.authentication.security.saml.SAMLManager;

public class SAMLStartupComponentTest {

    @Test
    public void testInitialize() throws Exception {
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = Mockito.mock(MetadataManager.class);
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        ConfigurationModel currentConfiguration = Mockito.mock(ConfigurationModel.class);
        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        Mockito.when(context.getCurrentConfiguration()).thenReturn(currentConfiguration);
        Mockito.when(context.isSAMLEnabled(Mockito.any(ConfigurationModel.class))).thenReturn(Boolean.TRUE.booleanValue());
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.eq(AuthenticationDescriptor.KEY_SAML_METADATA_URL))).thenReturn("metadataURL");
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.eq(AuthenticationDescriptor.KEY_SAML_ENTITY_ID))).thenReturn("entityId");
        Mockito.when(context.getFieldValueOrEmpty(Mockito.any(ConfigurationModel.class), Mockito.eq(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL))).thenReturn("baseURL");

        SAMLManager samlManager = new SAMLManager(parserPool, extendedMetadata, metadataManager, metadataGenerator, filePersistenceUtil, context);
        SAMLStartupComponent startupComponent = new SAMLStartupComponent(samlManager);
        startupComponent.initializeComponent();

        Mockito.verify(context).disableSAML();
    }

    @Test
    public void testInitializeException() throws Exception {
        SAMLContext context = Mockito.mock(SAMLContext.class);
        ParserPool parserPool = Mockito.mock(ParserPool.class);
        ExtendedMetadata extendedMetadata = Mockito.mock(ExtendedMetadata.class);
        MetadataManager metadataManager = Mockito.mock(MetadataManager.class);
        MetadataGenerator metadataGenerator = Mockito.mock(MetadataGenerator.class);
        FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        Mockito.when(context.getCurrentConfiguration()).thenThrow(new AlertConfigurationException("Test exception"));

        SAMLManager samlManager = new SAMLManager(parserPool, extendedMetadata, metadataManager, metadataGenerator, filePersistenceUtil, context);
        SAMLStartupComponent startupComponent = new SAMLStartupComponent(samlManager);
        startupComponent.initializeComponent();

        Mockito.verify(metadataGenerator, Mockito.times(0)).setEntityId(Mockito.anyString());
        Mockito.verify(metadataGenerator, Mockito.times(0)).setEntityBaseURL(Mockito.anyString());
        Mockito.verify(metadataManager, Mockito.times(0)).setProviders(Mockito.anyList());
        Mockito.verify(metadataManager, Mockito.times(0)).afterPropertiesSet();
    }

}
