package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SupportedChannelsTest {

    @Test
    public void testSupportedChannels() {
        assertEquals("email_group_channel", SupportedChannels.EMAIL_GROUP);
        assertEquals("hipchat_channel", SupportedChannels.HIPCHAT);
        assertEquals("slack_channel", SupportedChannels.SLACK);
    }

}
