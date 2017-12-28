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
package com.blackducksoftware.integration.hub.alert.mock.model;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.DistributionMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;
import com.google.gson.JsonObject;

public class MockSlackRestModel extends DistributionMockUtils implements MockRestModelUtil<SlackDistributionRestModel> {
    private final String webhook;
    private final String channelName;
    private final String channelUsername;
    private final String id;

    public MockSlackRestModel() {
        this("Webhook", "ChannelName", "ChannelUsername", "1");
    }

    private MockSlackRestModel(final String webhook, final String channelName, final String channelUsername, final String id) {
        super(id);
        this.webhook = webhook;
        this.channelName = channelName;
        this.channelUsername = channelUsername;
        this.id = id;
    }

    @Test
    public void test() throws JSONException {
        MockRestModelUtil.super.verifyEmptyRestModel();
        MockRestModelUtil.super.verifyRestModel();
    }

    public String getWebhook() {
        return webhook;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public SlackDistributionRestModel createRestModel() {
        final SlackDistributionRestModel restModel = new SlackDistributionRestModel(getCommonId(), webhook, channelUsername, channelName, getDistributionConfigId(), getDistributionType(), getName(), getFrequency(), getFilterByProject(),
                getProjects(), getNotifications());
        return restModel;
    }

    @Override
    public SlackDistributionRestModel createEmptyRestModel() {
        return new SlackDistributionRestModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("webhook", webhook);
        json.addProperty("channelUsername", channelUsername);
        json.addProperty("channelName", channelName);
        getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("webhook", null);
        json.add("channelUsername", null);
        json.add("channelName", null);
        getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

}
