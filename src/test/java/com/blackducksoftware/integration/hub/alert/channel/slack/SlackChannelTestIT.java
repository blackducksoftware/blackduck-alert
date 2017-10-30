/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;

public class SlackChannelTestIT extends RestChannelTest {

    @Test
    public void sendMessageTestIT() {
        final SlackChannel slackChannel = new SlackChannel(gson, null);
        final String roomName = properties.getProperty("slack.channel.name");
        final String username = properties.getProperty("slack.username");
        final String webHook = properties.getProperty("slack.web.hook");
        final SlackConfigEntity config = new SlackConfigEntity(roomName, username, webHook);
        final String actual = slackChannel.testMessage(config);
        final String expected = "200";
        Assert.assertEquals(expected, actual);
    }
}
