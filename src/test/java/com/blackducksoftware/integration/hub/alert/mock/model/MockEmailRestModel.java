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
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;
import com.google.gson.JsonObject;

public class MockEmailRestModel extends DistributionMockUtils implements MockRestModelUtil<EmailGroupDistributionRestModel> {
    private final String groupName;
    private final String id;

    public MockEmailRestModel() {
        this("groupName", "1");
    }

    private MockEmailRestModel(final String groupName, final String id) {
        super();
        this.groupName = groupName;
        this.id = id;
    }

    @Test
    public void test() throws JSONException {
        MockRestModelUtil.super.verifyEmptyRestModel();
        MockRestModelUtil.super.verifyRestModel();
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public EmailGroupDistributionRestModel createRestModel() {
        final EmailGroupDistributionRestModel restModel = new EmailGroupDistributionRestModel(getCommonId(), getDistributionConfigId(), getDistributionType(), getName(), getFrequency(), getFilterByProject(), groupName, getProjects(),
                getNotifications());
        return restModel;
    }

    @Override
    public EmailGroupDistributionRestModel createEmptyRestModel() {
        return new EmailGroupDistributionRestModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("groupName", null);
        getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

}
