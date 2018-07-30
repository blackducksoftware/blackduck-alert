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

import com.blackducksoftware.integration.alert.mock.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.google.gson.JsonObject;

public class MockHipChatGlobalRestModel extends MockGlobalRestModelUtil<HipChatGlobalConfig> {
    private final String apiKey;
    private final boolean apiKeyIsSet;
    private final String id;
    private final String hostServer;

    public MockHipChatGlobalRestModel() {
        this("ApiKey", false, "1", "HostServer");
    }

    private MockHipChatGlobalRestModel(final String apiKey, final boolean apiKeyIsSet, final String id, final String hostServer) {
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

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public HipChatGlobalConfig createGlobalRestModel() {
        final HipChatGlobalConfig restModel = new HipChatGlobalConfig(id, apiKey, apiKeyIsSet, hostServer);
        return restModel;
    }

    @Override
    public HipChatGlobalConfig createEmptyGlobalRestModel() {
        return new HipChatGlobalConfig();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("apiKey", apiKey);
        json.addProperty("apiKeyIsSet", apiKeyIsSet);
        json.addProperty("id", id);
        json.addProperty("hostServer", hostServer);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("apiKeyIsSet", apiKeyIsSet);
        return json.toString();
    }

}
