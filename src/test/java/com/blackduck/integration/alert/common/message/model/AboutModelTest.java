/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.message.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.blackduck.integration.alert.web.api.about.AboutModel;

 class AboutModelTest {
     @Test
     void testWithValues() {
         String version = "1.2.3";
         String created = "date";
         String description = "description";
         String projectUrl = "https://www.google.com";
         String commitHash = "abc123xyz";
         String copyrightYear = "year";
         String documentationUrl = "https://www.google.com";
         boolean initialized = true;
         String startupTime = "startup time is now";
         DescriptorMetadata providerMetadata = Mockito.mock(DescriptorMetadata.class);
        DescriptorMetadata channelMetadata = Mockito.mock(DescriptorMetadata.class);
        Set<DescriptorMetadata> providers = Set.of(providerMetadata);
        Set<DescriptorMetadata> channels = Set.of(channelMetadata);

         AboutModel model = new AboutModel(version, created, description, projectUrl, commitHash, copyrightYear, documentationUrl, initialized, startupTime, providers, channels);

        assertEquals(version, model.getVersion());
        assertEquals(created, model.getCreated());
        assertEquals(description, model.getDescription());
        assertEquals(projectUrl, model.getProjectUrl());
        assertEquals(commitHash, model.getCommitHash());
        assertEquals(copyrightYear, model.getCopyrightYear());
        assertEquals(documentationUrl, model.getDocumentationUrl());
        assertEquals(initialized, model.isInitialized());
        assertEquals(startupTime, model.getStartupTime());
        assertEquals(providers, model.getProviders());
        assertEquals(channels, model.getChannels());
    }

}
