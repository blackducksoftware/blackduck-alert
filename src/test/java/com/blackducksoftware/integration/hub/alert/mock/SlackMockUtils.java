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
package com.blackducksoftware.integration.hub.alert.mock;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;
import com.google.gson.JsonObject;

public class SlackMockUtils extends DistributionMockUtils implements MockUtils<SlackDistributionRestModel, ConfigRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity> {
    private final String webhook;
    private final String channelName;
    private final String channelUsername;
    private final String id;

    public SlackMockUtils() {
        this("Webhook", "ChannelName", "ChannelUsername", "1");
    }

    public SlackMockUtils(final String webhook, final String channelName, final String channelUsername, final String id) {
        super(id);
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
    public String getId() {
        return id;
    }

    @Override
    public SlackDistributionRestModel createRestModel() {
        final SlackDistributionRestModel restModel = new SlackDistributionRestModel(getCommonId(), webhook, channelUsername, channelName, getDistributionConfigId(), getDistributionType(), getName(), getFrequency(), getNotificationType(),
                getFilterByProject(), getProjects());
        return restModel;
    }

    @Override
    public SlackDistributionRestModel createEmptyRestModel() {
        return new SlackDistributionRestModel();
    }

    @Override
    public SlackDistributionConfigEntity createEntity() {
        final SlackDistributionConfigEntity configEntity = new SlackDistributionConfigEntity(webhook, channelUsername, channelName);
        configEntity.setId(Long.parseLong(id));
        return configEntity;
    }

    @Override
    public SlackDistributionConfigEntity createEmptyEntity() {
        return new SlackDistributionConfigEntity();
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

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("webhook", webhook);
        json.addProperty("channelUsername", channelUsername);
        json.addProperty("channelName", channelName);
        json.addProperty("id", Long.valueOf(id));
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

    /*
     * These types exist but have no real definition
     */

    @Override
    @Deprecated
    public GlobalSlackConfigEntity createGlobalEntity() {
        return new GlobalSlackConfigEntity();
    }

    @Override
    @Deprecated
    public GlobalSlackConfigEntity createEmptyGlobalEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public String getGlobalEntityJson() {
        return "";
    }

    @Override
    @Deprecated
    public String getEmptyGlobalEntityJson() {
        return "";
    }

    /*
     * End
     */

    /*
     * Slack doesn't need a global rest model thus it doesn't exist
     */

    @Override
    @Deprecated
    public ConfigRestModel createGlobalRestModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public ConfigRestModel createEmptyGlobalRestModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public String getGlobalRestModelJson() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public String getEmptyGlobalRestModelJson() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * End
     */

}
