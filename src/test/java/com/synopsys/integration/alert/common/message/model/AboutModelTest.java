package com.synopsys.integration.alert.common.message.model;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.web.api.about.AboutModel;

public class AboutModelTest {
    @Test
    public void testWithValues() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String projectUrl = "https://www.google.com";
        String documentationUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        DescriptorMetadata providerMetadata = Mockito.mock(DescriptorMetadata.class);
        DescriptorMetadata channelMetadata = Mockito.mock(DescriptorMetadata.class);
        Set<DescriptorMetadata> providers = Set.of(providerMetadata);
        Set<DescriptorMetadata> channels = Set.of(channelMetadata);

        AboutModel model = new AboutModel(version, created, description, projectUrl, documentationUrl, initialized, startupTime, providers, channels);

        assertEquals(version, model.getVersion());
        assertEquals(description, model.getDescription());
        assertEquals(projectUrl, model.getProjectUrl());
        assertEquals(documentationUrl, model.getDocumentationUrl());
        assertEquals(initialized, model.isInitialized());
        assertEquals(startupTime, model.getStartupTime());
        assertEquals(providers, model.getProviders());
        assertEquals(channels, model.getChannels());
    }

}
