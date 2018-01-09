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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.mock;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.MockCommonDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.google.gson.JsonObject;

public class MockHipChatRestModel extends MockRestModelUtil<HipChatDistributionRestModel> {
    private final MockCommonDistributionRestModel distributionMockUtil = new MockCommonDistributionRestModel();

    private final String roomId;
    private final String notify;
    private final String color;
    private final String id;

    public MockHipChatRestModel() {
        this("11", "false", "black", "1");
    }

    private MockHipChatRestModel(final String roomId, final String notify, final String color, final String id) {
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
        this.id = id;
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
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public HipChatDistributionRestModel createRestModel() {
        final HipChatDistributionRestModel restModel = new HipChatDistributionRestModel(String.valueOf(distributionMockUtil.getId()), roomId, notify, color, distributionMockUtil.getDistributionConfigId(),
                distributionMockUtil.getDistributionType(), distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), distributionMockUtil.getProjects(),
                distributionMockUtil.getNotifications());
        return restModel;
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("roomId", roomId);
        json.addProperty("notify", notify);
        json.addProperty("color", color);

        return distributionMockUtil.combineWithRestModelJson(json);
    }

    @Override
    public HipChatDistributionRestModel createEmptyRestModel() {
        return new HipChatDistributionRestModel();
    }

}
