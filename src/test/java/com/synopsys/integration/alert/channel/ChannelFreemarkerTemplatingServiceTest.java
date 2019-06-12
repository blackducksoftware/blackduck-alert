package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ChannelFreemarkerTemplatingServiceTest {

    @Test
    public void testExpectedDirectoryPaths() {
        final ChannelFreemarkerTemplatingService channelFreemarkerTemplatingService = new ChannelFreemarkerTemplatingService();

        final String testChannel = "testChannel";
        final String directory = "directory";

        final String templatePath = channelFreemarkerTemplatingService.getTemplatePath(directory, testChannel);
        assertEquals(directory + "/" + testChannel, templatePath);

        final String templatePathMissingDirectory = channelFreemarkerTemplatingService.getTemplatePath("", testChannel);
        final String userDirectory = System.getProperties().getProperty("user.dir");
        assertEquals(userDirectory + "/src/main/resources/" + testChannel + "/templates", templatePathMissingDirectory);
    }
}
