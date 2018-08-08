package com.blackducksoftware.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.alert.web.model.AboutModel;

public class AboutModelTest {

    @Test
    public void testWithValues() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final List<String> channelList = Arrays.asList("channel_1", "channel_2", "channel_3");
        final List<String> providerList = Arrays.asList("provider_1", "provider_2", "provider_3");

        final AboutModel model = new AboutModel(version, description, gitHubUrl, providerList, channelList);

        assertEquals(version, model.getVersion());
        assertEquals(description, model.getDescription());
        assertEquals(gitHubUrl, model.getProjectUrl());
        assertEquals(providerList, model.getProviderList());
        assertEquals(channelList, model.getChannelList());
    }
}
