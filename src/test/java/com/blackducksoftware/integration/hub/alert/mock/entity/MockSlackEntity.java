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
package com.blackducksoftware.integration.hub.alert.mock.entity;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.google.gson.JsonObject;

public class MockSlackEntity implements MockEntityUtil<SlackDistributionConfigEntity> {
    private final String webhook;
    private final String channelName;
    private final String channelUsername;
    private final Long id;

    public MockSlackEntity() {
        this("Webhook", "ChannelName", "ChannelUsername", 1L);
    }

    public MockSlackEntity(final String webhook, final String channelName, final String channelUsername, final Long id) {
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
        return id;
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
        json.add("webhook", null);
        json.addProperty("channelUsername", "Hub-alert");
        json.add("channelName", null);
        json.add("id", null);
        return json.toString();
    }
}
