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
package com.blackducksoftware.integration.alert.channel.slack.mock;

import com.blackducksoftware.integration.alert.mock.model.MockCommonDistributionRestModel;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.channel.model.SlackDistributionRestModel;
import com.google.gson.JsonObject;

public class MockSlackRestModel extends MockRestModelUtil<SlackDistributionRestModel> {
    private final MockCommonDistributionRestModel distributionMockUtil = new MockCommonDistributionRestModel();

    private String webhook;
    private String channelName;
    private String channelUsername;
    private String id;

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

    public void setWebhook(final String webhook) {
        this.webhook = webhook;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(final String channelName) {
        this.channelName = channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public void setChannelUsername(final String channelUsername) {
        this.channelUsername = channelUsername;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public SlackDistributionRestModel createRestModel() {
        final SlackDistributionRestModel restModel = new SlackDistributionRestModel(String.valueOf(distributionMockUtil.getId()), webhook, channelUsername, channelName, distributionMockUtil.getDistributionConfigId(),
                distributionMockUtil.getDistributionType(), distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), distributionMockUtil.getProjects(),
                distributionMockUtil.getNotificationsAsStrings());
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
