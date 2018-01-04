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

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.google.gson.JsonObject;

public class MockEmailEntity extends MockEntityUtil<EmailGroupDistributionConfigEntity> {
    private final String groupName;
    private final Long id;

    public MockEmailEntity() {
        this("groupName", 1L);
    }

    private MockEmailEntity(final String groupName, final Long id) {
        super();
        this.groupName = groupName;
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEntity() {
        final EmailGroupDistributionConfigEntity entity = new EmailGroupDistributionConfigEntity(groupName);
        entity.setId(id);
        return entity;
    }

    @Override
    public EmailGroupDistributionConfigEntity createEmptyEntity() {
        return new EmailGroupDistributionConfigEntity();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

}
