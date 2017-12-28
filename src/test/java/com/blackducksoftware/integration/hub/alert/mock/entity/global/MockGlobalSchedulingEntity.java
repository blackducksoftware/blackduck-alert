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

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.google.gson.JsonObject;

public class MockGlobalSchedulingEntity implements MockGlobalEntityUtil<GlobalSchedulingConfigEntity> {
    private final String accumulatorCron;
    private final String dailyDigestCron;
    private final String purgeDataCron;
    private final Long id;

    public MockGlobalSchedulingEntity() {
        this("1 1 1 1 1 1", "2 2 2 2 2 2", "3 3 3 3 3 3", 1L);
    }

    private MockGlobalSchedulingEntity(final String accumulatorCron, final String dailyDigestCron, final String purgeDataCron, final Long id) {
        super();
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
        this.purgeDataCron = purgeDataCron;
        this.id = id;
    }

    @Test
    public void test() throws JSONException {
        MockGlobalEntityUtil.super.verifyEmptyGlobalEntity();
        MockGlobalEntityUtil.super.verifyGlobalEntity();
    }

    public String getAccumulatorCron() {
        return accumulatorCron;
    }

    public String getDailyDigestCron() {
        return dailyDigestCron;
    }

    public String getPurgeDataCron() {
        return purgeDataCron;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalSchedulingConfigEntity createGlobalEntity() {
        final GlobalSchedulingConfigEntity entity = new GlobalSchedulingConfigEntity(accumulatorCron, dailyDigestCron, purgeDataCron);
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalSchedulingConfigEntity createEmptyGlobalEntity() {
        return new GlobalSchedulingConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("accumulatorCron", accumulatorCron);
        json.addProperty("dailyDigestCron", dailyDigestCron);
        json.addProperty("purgeDataCron", purgeDataCron);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("accumulatorCron", null);
        json.add("dailyDigestCron", null);
        json.add("purgeDataCron", null);
        json.add("id", null);
        return json.toString();
    }

}
