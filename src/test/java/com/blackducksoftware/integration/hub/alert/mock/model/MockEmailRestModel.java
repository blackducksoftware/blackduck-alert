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

import com.blackducksoftware.integration.hub.alert.mock.DistributionMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;
import com.google.gson.JsonObject;

public class MockEmailRestModel extends MockRestModelUtil<EmailGroupDistributionRestModel> {
    private final DistributionMockUtils distributionMockUtil = new DistributionMockUtils("1");

    private final String groupName;
    private final String id;

    public MockEmailRestModel() {
        this("groupName", "1");
    }

    private MockEmailRestModel(final String groupName, final String id) {
        this.groupName = groupName;
        this.id = id;
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
        final EmailGroupDistributionRestModel restModel = new EmailGroupDistributionRestModel(distributionMockUtil.getCommonId(), distributionMockUtil.getDistributionConfigId(), distributionMockUtil.getDistributionType(),
                distributionMockUtil.getName(), distributionMockUtil.getFrequency(), distributionMockUtil.getFilterByProject(), groupName, distributionMockUtil.getProjects(), distributionMockUtil.getNotifications());
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
        distributionMockUtil.getDistributionRestModelJson(json);
        return json.toString();
    }

    @Override
    public String getEmptyRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("groupName", null);
        distributionMockUtil.getEmptyDistributionRestModelJson(json);
        return json.toString();
    }

}
