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
package com.blackducksoftware.integration.hub.alert.mock.entity.global;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.google.gson.JsonObject;

public class MockGlobalHubEntity extends MockGlobalEntityUtil<GlobalHubConfigEntity> {
    private final Integer hubTimeout;
    private final String hubUsername;
    private final String hubPassword;
    private final Long id;

    public MockGlobalHubEntity() {
        this(444, "HubUsername", "HubPassword", 1L);
    }

    private MockGlobalHubEntity(final Integer hubTimeout, final String hubUsername, final String hubPassword, final Long id) {
        super();
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.id = id;
    }

    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalHubConfigEntity createGlobalEntity() {
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(Integer.valueOf(hubTimeout), hubUsername, hubPassword);
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
        json.addProperty("hubUsername", hubUsername);
        json.addProperty("id", id);
        return json.toString();
    }

}
