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
package com.blackducksoftware.integration.hub.alert.hub.mock;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.google.gson.JsonObject;

public class MockGlobalHubEntity extends MockGlobalEntityUtil<GlobalHubConfigEntity> {
    private final Integer hubTimeout;
    private final String hubApiKey;
    private final Long id;

    public MockGlobalHubEntity() {
        this(444, "HubApiKey", 1L);
    }

    private MockGlobalHubEntity(final Integer hubTimeout, final String hubApiKey, final Long id) {
        super();
        this.hubTimeout = hubTimeout;
        this.hubApiKey = hubApiKey;
        this.id = id;
    }

    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public String getHubApiKey() {
        return hubApiKey;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalHubConfigEntity createGlobalEntity() {
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(Integer.valueOf(hubTimeout), hubApiKey);
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalHubConfigEntity createEmptyGlobalEntity() {
        return new GlobalHubConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubTimeout", hubTimeout);
        json.addProperty("id", id);
        return json.toString();
    }

}
