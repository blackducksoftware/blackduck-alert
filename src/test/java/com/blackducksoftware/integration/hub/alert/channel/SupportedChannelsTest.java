package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// Extends SupportedChannels so that it counts towards the code coverage
public class SupportedChannelsTest extends SupportedChannels {

    @Test
    public void testSupportedChannels() {
        assertEquals("email_group_channel", EMAIL_GROUP);
        assertEquals("hipchat_channel", HIPCHAT);
        assertEquals("slack_channel", SLACK);
    }

}
