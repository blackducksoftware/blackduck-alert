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
package com.blackducksoftware.integration.hub.alert.mock.model.global;

import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;
import com.google.gson.JsonObject;

public class MockGlobalSchedulingRestModel extends MockGlobalRestModelUtil<GlobalSchedulingConfigRestModel> {
    private final String accumulatorCron;
    private final String dailyDigestCron;
    private final String purgeDataCron;
    private final String id;

    public MockGlobalSchedulingRestModel() {
        this("1 1 1 1 1 1", "2 2 2 2 2 2", "3 3 3 3 3 3", "1");
    }

    private MockGlobalSchedulingRestModel(final String accumulatorCron, final String dailyDigestCron, final String purgeDataCron, final String id) {
        super();
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
        this.purgeDataCron = purgeDataCron;
        this.id = id;
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
        return Long.valueOf(id);
    }

    @Override
    public GlobalSchedulingConfigRestModel createGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel(id, accumulatorCron, dailyDigestCron, purgeDataCron);
    }

    @Override
    public GlobalSchedulingConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("accumulatorCron", accumulatorCron);
        json.addProperty("dailyDigestCron", dailyDigestCron);
        json.addProperty("purgeDataCron", purgeDataCron);
        json.addProperty("id", id);
        return json.toString();
    }

}
