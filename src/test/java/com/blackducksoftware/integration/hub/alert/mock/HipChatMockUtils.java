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

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;
import com.google.gson.JsonObject;

public class HipChatMockUtils extends DistributionMockUtils implements MockUtils<HipChatDistributionRestModel, GlobalHipChatConfigRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {
    private final String apiKey;
    private final boolean apiKeyIsSet;
    private final String roomId;
    private final String notify;
    private final String color;
    private final String id;

    public HipChatMockUtils() {
        this("ApiKey", false, "11", "false", "black", "1");
    }

    public HipChatMockUtils(final String apiKey, final boolean apiKeyIsSet, final String roomId, final String notify, final String color, final String id) {
        super(id);
        this.apiKey = apiKey;
        this.apiKeyIsSet = apiKeyIsSet;
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String getId() {
        return id;
    }

    /*
     * Global REST configurations
     */

    @Override
    public GlobalHipChatConfigRestModel createGlobalRestModel() {
        final GlobalHipChatConfigRestModel restModel = new GlobalHipChatConfigRestModel(id, apiKey, apiKeyIsSet);
        return restModel;
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("apiKeyIsSet", apiKeyIsSet);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("apiKeyIsSet", apiKeyIsSet);
        json.add("id", null);
        return json.toString();
    }

    /*
     * Global Entity configurations
     */

    @Override
    public GlobalHipChatConfigEntity createGlobalEntity() {
        final GlobalHipChatConfigEntity configEntity = new GlobalHipChatConfigEntity(apiKey);
        configEntity.setId(Long.parseLong(id));
        return configEntity;
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("id", null);
        return json.toString();
    }

    /*
     * Distribution REST configurations
     */

    @Override
    public HipChatDistributionRestModel createRestModel() {
        final HipChatDistributionRestModel hipChatDistributionRestModel = new HipChatDistributionRestModel(getCommonId(), roomId, notify, color, getDistributionConfigId(), getDistributionType(), getName(), getFrequency(),
                getFilterByProject(), getProjects(), getNotificationsAsStrings());
        return hipChatDistributionRestModel;
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("roomId", roomId);
        json.addProperty("notify", notify);
        json.addProperty("color", color);
        getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("roomId", null);
        json.add("notify", null);
        json.add("color", null);
        getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

    /*
     * Distribution Entity configurations
     */

    @Override
    public HipChatDistributionConfigEntity createEntity() {
        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = new HipChatDistributionConfigEntity(Integer.parseInt(roomId), Boolean.parseBoolean(notify), color);
        hipChatDistributionConfigEntity.setId(Long.parseLong(id));
        return hipChatDistributionConfigEntity;
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("roomId", Integer.valueOf(roomId));
        json.addProperty("notify", Boolean.valueOf(notify));
        json.addProperty("color", color);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("roomId", null);
        json.add("notify", null);
        json.add("color", null);
        json.add("id", null);
        return json.toString();
    }

    @Override
    public GlobalHipChatConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalHipChatConfigRestModel();
    }

    @Override
    public HipChatDistributionRestModel createEmptyRestModel() {
        return new HipChatDistributionRestModel();
    }

    @Override
    public GlobalHipChatConfigEntity createEmptyGlobalEntity() {
        return new GlobalHipChatConfigEntity();
    }

    @Override
    public HipChatDistributionConfigEntity createEmptyEntity() {
        return new HipChatDistributionConfigEntity();
    }

}
