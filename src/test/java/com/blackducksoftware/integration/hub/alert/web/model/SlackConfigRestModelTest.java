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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

public class SlackConfigRestModelTest {

    // @Test
    public void testEmptyModel() {
        final SlackDistributionRestModel slackModel = new SlackDistributionRestModel();
        assertEquals(-3032738984577328749L, SlackDistributionRestModel.getSerialversionuid());

        assertNull(slackModel.getChannelName());
        assertNull(slackModel.getChannelUsername());
        assertNull(slackModel.getWebhook());
        assertNull(slackModel.getId());

        final int restModelHash = slackModel.hashCode();
        assertEquals(-2120005431, restModelHash);

        final String expectedString = "{\"id\":null}";
        assertEquals(expectedString, slackModel.toString());

        final SlackDistributionRestModel slackModelNew = new SlackDistributionRestModel();
        assertEquals(slackModel, slackModelNew);
    }

    // @Test
    // public void testModel() {
    // final String id = "Id";
    // final String apiKey = "ApiKey";
    //
    // final GlobalHipChatConfigRestModel hipChatConfigRestModel = new GlobalHipChatConfigRestModel(id, apiKey);
    //
    // assertEquals(apiKey, hipChatConfigRestModel.getApiKey());
    // assertEquals(id, hipChatConfigRestModel.getId());
    //
    // final int restModelHash = hipChatConfigRestModel.hashCode();
    // assertEquals(-215714083, restModelHash);
    //
    // final String expectedString = "{\"id\":\"Id\"}";
    // assertEquals(expectedString, hipChatConfigRestModel.toString());
    //
    // final GlobalHipChatConfigRestModel hipChatConfigRestModelNew = new GlobalHipChatConfigRestModel(id, apiKey);
    // assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    // }
}
