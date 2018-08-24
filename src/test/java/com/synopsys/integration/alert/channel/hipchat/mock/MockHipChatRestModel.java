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
package com.synopsys.integration.alert.channel.hipchat.mock;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.mock.model.MockCommonDistributionRestModel;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;

public class MockHipChatRestModel extends MockRestModelUtil<HipChatDistributionConfig> {
    private final MockCommonDistributionRestModel distributionMockUtil = new MockCommonDistributionRestModel();

    private String roomId;
    private boolean notify;
    private String color;
    private String id;

    public MockHipChatRestModel() {
        this("11", false, "yellow", "1");
    }

    private MockHipChatRestModel(final String roomId, final boolean notify, final String color, final String id) {
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(final String roomId) {

        this.roomId = roomId;
    }

    public boolean getNotify() {
        return notify;
    }

    public void setNotify(final boolean notify) {

        this.notify = notify;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    public void setId(final String id) {

        this.id = id;
    }

    @Override
    public HipChatDistributionConfig createRestModel() {
        final HipChatDistributionConfig restModel = new HipChatDistributionConfig(String.valueOf(distributionMockUtil.getId()), roomId, notify, color, distributionMockUtil.getDistributionConfigId(),
                distributionMockUtil.getDistributionType(), distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), distributionMockUtil.getProjects(),
                distributionMockUtil.getNotificationsAsStrings());
        return restModel;
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("notify", false);
        return json.toString();
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
    public HipChatDistributionConfig createEmptyRestModel() {
        return new HipChatDistributionConfig();
    }
}
