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

import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.google.gson.Gson;

public class SlackChannelTestIT {

    @Test
    public void sendMessageTestIT() {
        final Gson gson = new Gson();
        final SlackChannel slackChannel = new SlackChannel(gson);
        final SlackConfigEntity config = new SlackConfigEntity("#alert", "webhookbot", "https://hooks.slack.com/services/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");
        final String actual = slackChannel.testMessage(config);
        final String expected = "200";
        Assert.assertEquals(expected, actual);
    }
}
