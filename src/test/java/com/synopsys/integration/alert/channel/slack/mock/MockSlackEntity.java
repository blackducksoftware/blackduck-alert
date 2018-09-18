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
package com.synopsys.integration.alert.channel.slack.mock;

import java.util.Collections;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class MockSlackEntity extends MockEntityUtil<SlackDistributionConfigEntity> {
    private String webhook;
    private String channelName;
    private String channelUsername;
    private Long id;

    public MockSlackEntity() {
        this("Webhook", "ChannelName", "ChannelUsername", 1L);
    }

    private MockSlackEntity(final String webhook, final String channelName, final String channelUsername, final Long id) {
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
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Config createConfig() {
        return new SlackDistributionConfig(id.toString(), webhook, channelUsername, channelName, "0L", SlackChannel.COMPONENT_NAME, "SlackTest", BlackDuckProvider.COMPONENT_NAME, "real_time", "false", Collections.emptyList(),
            Collections.emptyList(), "DEFAULT");
    }

    @Override
    public Config createEmptyConfig() {
        return new SlackDistributionConfig();
    }

    @Override
    public SlackDistributionConfigEntity createEntity() {
        final SlackDistributionConfigEntity configEntity = new SlackDistributionConfigEntity(webhook, channelUsername, channelName);
        configEntity.setId(id);
        return configEntity;
    }

    @Override
    public SlackDistributionConfigEntity createEmptyEntity() {
        return new SlackDistributionConfigEntity();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("webhook", webhook);
        json.addProperty("channelUsername", channelUsername);
        json.addProperty("channelName", channelName);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("channelUsername", "BlackDuck-Alert");
        return json.toString();
    }
}
