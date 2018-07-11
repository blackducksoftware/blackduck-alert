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
package com.blackducksoftware.integration.alert.scheduling.controller;

import com.blackducksoftware.integration.alert.mock.model.global.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingConfigRestModel;
import com.google.gson.JsonObject;

public class MockGlobalSchedulingRestModel extends MockGlobalRestModelUtil<GlobalSchedulingConfigRestModel> {
    private final String accumulatorNextRun;
    private final String dailyDigestHourOfDay;
    private final String dailyDigestNextRun;
    private final String purgeDataFrequencyDays;
    private final String purgeDataNextRun;
    private final String id;

    public MockGlobalSchedulingRestModel() {
        this("33", "2", "01/19/2018 02:00 AM UTC", "5", "01/21/2018 12:00 AM UTC", "1");
    }

    private MockGlobalSchedulingRestModel(final String accumulatorNextRun, final String dailyDigestHourOfDay, final String dailyDigestNextRun, final String purgeDataFrequencyDays, final String purgeDataNextRun, final String id) {
        this.accumulatorNextRun = accumulatorNextRun;
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
        this.dailyDigestNextRun = dailyDigestNextRun;
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
        this.purgeDataNextRun = purgeDataNextRun;
        this.id = id;
    }

    public String getAccumulatorNextRun() {
        return accumulatorNextRun;
    }

    public String getDailyDigestHourOfDay() {
        return dailyDigestHourOfDay;
    }

    public String getDailyDigestNextRun() {
        return dailyDigestNextRun;
    }

    public String getPurgeDataFrequencyDays() {
        return purgeDataFrequencyDays;
    }

    public String getPurgeDataNextRun() {
        return purgeDataNextRun;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalSchedulingConfigRestModel createGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel(id, accumulatorNextRun, dailyDigestHourOfDay, dailyDigestNextRun, purgeDataFrequencyDays, purgeDataNextRun);
    }

    @Override
    public GlobalSchedulingConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalSchedulingConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("accumulatorNextRun", accumulatorNextRun);
        json.addProperty("dailyDigestHourOfDay", dailyDigestHourOfDay);
        json.addProperty("dailyDigestNextRun", dailyDigestNextRun);
        json.addProperty("purgeDataFrequencyDays", purgeDataFrequencyDays);
        json.addProperty("purgeDataNextRun", purgeDataNextRun);
        json.addProperty("id", id);
        return json.toString();
    }

}
