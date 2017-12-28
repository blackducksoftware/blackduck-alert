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

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.google.gson.JsonObject;

public class MockHipChatGlobalEntity implements MockGlobalEntityUtil<GlobalHipChatConfigEntity> {
    private final String apiKey;
    private final boolean apiKeyIsSet;
    private final Long id;

    public MockHipChatGlobalEntity() {
        this("ApiKey", false, 1L);
    }

    private MockHipChatGlobalEntity(final String apiKey, final boolean apiKeyIsSet, final Long id) {
        this.apiKey = apiKey;
        this.apiKeyIsSet = apiKeyIsSet;
        this.id = id;
    }

    @Test
    public void test() throws JSONException {
        MockGlobalEntityUtil.super.verifyEmptyGlobalEntity();
        MockGlobalEntityUtil.super.verifyGlobalEntity();
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isApiKeyIsSet() {
        return apiKeyIsSet;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalHipChatConfigEntity createGlobalEntity() {
        final GlobalHipChatConfigEntity configEntity = new GlobalHipChatConfigEntity(apiKey);
        configEntity.setId(id);
        return configEntity;
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("id", null);
        return json.toString();
    }

    @Override
    public GlobalHipChatConfigEntity createEmptyGlobalEntity() {
        return new GlobalHipChatConfigEntity();
    }

}
