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
package com.blackducksoftware.integration.hub.alert.channel.slack.mock;

import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.MockCommonDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.google.gson.JsonObject;

public class MockSlackRestModel extends MockRestModelUtil<SlackDistributionRestModel> {
    private final MockCommonDistributionRestModel distributionMockUtil = new MockCommonDistributionRestModel();

    private final String webhook;
    private final String channelName;
    private final String channelUsername;
    private final String id;

    public MockSlackRestModel() {
        this("Webhook", "ChannelName", "ChannelUsername", "1");
    }

    private MockSlackRestModel(final String webhook, final String channelName, final String channelUsername, final String id) {
        this.webhook = webhook;
        this.channelName = channelName;
        this.channelUsername = channelUsername;
        this.id = id;
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
        final SlackDistributionRestModel restModel = new SlackDistributionRestModel(String.valueOf(distributionMockUtil.getId()), webhook, channelUsername, channelName, distributionMockUtil.getDistributionConfigId(),
                distributionMockUtil.getDistributionType(), distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), distributionMockUtil.getProjects(),
                distributionMockUtil.getNotifications());
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

        return distributionMockUtil.combineWithRestModelJson(json);
    }

}
