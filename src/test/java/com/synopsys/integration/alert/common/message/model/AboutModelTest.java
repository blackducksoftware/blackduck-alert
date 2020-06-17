package com.synopsys.integration.alert.common.message.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutModelTest {

    @Test
    public void testWithValues() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String gitHubUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        List<String> providers = List.of("provider_key");
        List<String> channels = List.of("channel_key");

        AboutModel model = new AboutModel(version, created, description, gitHubUrl, initialized, startupTime, providers, channels);

        assertEquals(version, model.getVersion());
        assertEquals(description, model.getDescription());
        assertEquals(gitHubUrl, model.getProjectUrl());
        assertEquals(initialized, model.isInitialized());
        assertEquals(startupTime, model.getStartupTime());
        assertEquals(providers, model.getProviders());
        assertEquals(channels, model.getChannels());
    }
}
