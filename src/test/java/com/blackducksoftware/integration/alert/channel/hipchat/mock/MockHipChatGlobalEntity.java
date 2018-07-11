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
package com.blackducksoftware.integration.alert.channel.hipchat.mock;

import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.alert.mock.entity.global.MockGlobalEntityUtil;
import com.google.gson.JsonObject;

public class MockHipChatGlobalEntity extends MockGlobalEntityUtil<GlobalHipChatConfigEntity> {
    private String apiKey;
    private boolean apiKeyIsSet;
    private Long id;
    private String hostServer;

    public MockHipChatGlobalEntity() {
        this("ApiKey", false, 1L, "HostServer");
    }

    private MockHipChatGlobalEntity(final String apiKey, final boolean apiKeyIsSet, final Long id, final String hostServer) {
        this.apiKey = apiKey;
        this.apiKeyIsSet = apiKeyIsSet;
        this.id = id;
        this.hostServer = hostServer;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isApiKeyIsSet() {
        return apiKeyIsSet;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKeyIsSet(final boolean apiKeyIsSet) {
        this.apiKeyIsSet = apiKeyIsSet;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getHostServer() {
        return hostServer;
    }

    public void setHostServer(final String hostServer) {
        this.hostServer = hostServer;
    }

    @Override
    public GlobalHipChatConfigEntity createGlobalEntity() {
        final GlobalHipChatConfigEntity configEntity = new GlobalHipChatConfigEntity(apiKey, hostServer);
        configEntity.setId(id);
        return configEntity;
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("hostServer", hostServer);
        return json.toString();
    }

    @Override
    public GlobalHipChatConfigEntity createEmptyGlobalEntity() {
        return new GlobalHipChatConfigEntity();
    }

}
