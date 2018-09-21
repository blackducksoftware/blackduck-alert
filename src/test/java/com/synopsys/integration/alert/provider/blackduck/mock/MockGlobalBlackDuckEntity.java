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
package com.synopsys.integration.alert.provider.blackduck.mock;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.mock.MockGlobalEntityUtil;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;

public class MockGlobalBlackDuckEntity extends MockGlobalEntityUtil<GlobalBlackDuckConfigEntity> {
    private Integer blackDuckTimeout;
    private String blackDuckApiKey;
    private String blackDuckUrl;
    private Long id;

    public MockGlobalBlackDuckEntity() {
        this(444, "BlackDuckApiKey############################################################", "http://localhost:443", 1L);
    }

    private MockGlobalBlackDuckEntity(final Integer blackDuckTimeout, final String blackDuckApiKey, final String blackDuckUrl, final Long id) {
        super();
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckApiKey = blackDuckApiKey;
        this.blackDuckUrl = blackDuckUrl;
        this.id = id;
    }

    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public void setBlackDuckTimeout(final Integer blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    public void setBlackDuckApiKey(final String blackDuckApiKey) {
        this.blackDuckApiKey = blackDuckApiKey;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Config createGlobalConfig() {
        return new BlackDuckConfig(id.toString(), blackDuckUrl, blackDuckTimeout.toString(), blackDuckApiKey, null != blackDuckApiKey, null, null, null,
            null, false, "true");
    }

    @Override
    public Config createEmptyGlobalConfig() {
        return new BlackDuckConfig();
    }

    @Override
    public GlobalBlackDuckConfigEntity createGlobalEntity() {
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(Integer.valueOf(blackDuckTimeout), blackDuckApiKey, blackDuckUrl);
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalBlackDuckConfigEntity createEmptyGlobalEntity() {
        return new GlobalBlackDuckConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("blackDuckTimeout", blackDuckTimeout);
        json.addProperty("blackDuckUrl", blackDuckUrl);
        json.addProperty("id", id);
        return json.toString();
    }

}
