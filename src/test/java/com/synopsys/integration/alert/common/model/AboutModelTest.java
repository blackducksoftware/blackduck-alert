package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.web.model.AboutDescriptorModel;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutModelTest {

    @Test
    public void testWithValues() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final List<AboutDescriptorModel> channelList = Arrays.asList(new AboutDescriptorModel("", "channel_1"), new AboutDescriptorModel("", "channel_2"), new AboutDescriptorModel("", "channel_3"));
        final List<AboutDescriptorModel> providerList = Arrays.asList(new AboutDescriptorModel("", "provider_1"), new AboutDescriptorModel("", "provider_2"), new AboutDescriptorModel("", "provider_3"));

        final AboutModel model = new AboutModel(version, description, gitHubUrl, providerList, channelList);

        assertEquals(version, model.getVersion());
        assertEquals(description, model.getDescription());
        assertEquals(gitHubUrl, model.getProjectUrl());
        assertEquals(providerList, model.getProviderList());
        assertEquals(channelList, model.getChannelList());
    }
}
