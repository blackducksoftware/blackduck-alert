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

public class HipChatMockUtils extends DistributionMockUtils implements MockUtils<HipChatDistributionRestModel, GlobalHipChatConfigRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {
    private final String apiKey;
    private final String roomId;
    private final String notify;
    private final String color;
    private final String id;

    public HipChatMockUtils() {
        this("ApiKey", "11", "false", "black", "1");
    }

    public HipChatMockUtils(final String apiKey, final String roomId, final String notify, final String color, final String id) {
        super(id);
        this.apiKey = apiKey;
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

    public String getId() {
        return id;
    }

    /*
     * Global REST configurations
     */

    @Override
    public GlobalHipChatConfigRestModel createGlobalRestModel() {
        final GlobalHipChatConfigRestModel restModel = new GlobalHipChatConfigRestModel(id, apiKey);
        return restModel;
    }

    @Override
    public String getGlobalRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"id\":\"");
        json.append(id);
        json.append("\"}");
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        return "{\"id\":null}";
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
        final StringBuilder json = new StringBuilder();
        json.append("{\"id\":");
        json.append(id);
        json.append("}");
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        return "{\"id\":null}";
    }

    /*
     * Distribution REST configurations
     */

    @Override
    public HipChatDistributionRestModel createRestModel() {
        final HipChatDistributionRestModel hipChatDistributionRestModel = new HipChatDistributionRestModel(getCommonId(), roomId, notify, color, getDistributionConfigId(), getDistributionType(), getName(), getFrequency(),
                getNotificationType(), getFilterByProject());
        return hipChatDistributionRestModel;
    }

    @Override
    public String getRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"roomId\":\"");
        json.append(roomId);
        json.append("\",\"notify\":\"");
        json.append(notify);
        json.append("\",\"color\":\"");
        json.append(color);
        json.append("\",");
        json.append(getDistributionRestModelJson());
        json.append("\"}");
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{\"roomId\":null,\"notify\":null,\"color\":null,");
        json.append(getEmptyDistributionRestModelJson());
        json.append("}");
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
        final StringBuilder json = new StringBuilder();
        json.append("{\"roomId\":");
        json.append(roomId);
        json.append(",\"notify\":");
        json.append(notify);
        json.append(",\"color\":\"");
        json.append(color);
        json.append("\",\"id\":");
        json.append(id);
        json.append("}");
        return json.toString();
    }

    @Override
    public String getEmptyEntityJson() {
        return "{\"roomId\":null,\"notify\":null,\"color\":null,\"id\":null}";
    }

}
