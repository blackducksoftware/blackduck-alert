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
package com.synopsys.integration.alert.web.scheduling.model;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.mock.MockGlobalRestModelUtil;
import com.synopsys.integration.alert.web.component.scheduling.SchedulingConfig;

public class MockGlobalSchedulingRestModel extends MockGlobalRestModelUtil<SchedulingConfig> {
    private String accumulatorNextRun;
    private String dailyDigestHourOfDay;
    private String dailyDigestNextRun;
    private String purgeDataFrequencyDays;
    private String purgeDataNextRun;
    private String id;

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

    public void setAccumulatorNextRun(final String accumulatorNextRun) {
        this.accumulatorNextRun = accumulatorNextRun;
    }

    public void setDailyDigestHourOfDay(final String dailyDigestHourOfDay) {
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
    }

    public void setDailyDigestNextRun(final String dailyDigestNextRun) {
        this.dailyDigestNextRun = dailyDigestNextRun;
    }

    public void setPurgeDataFrequencyDays(final String purgeDataFrequencyDays) {
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
    }

    public void setPurgeDataNextRun(final String purgeDataNextRun) {
        this.purgeDataNextRun = purgeDataNextRun;
    }

    public void setId(final String id) {
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
    public SchedulingConfig createGlobalRestModel() {
        return new SchedulingConfig(id, accumulatorNextRun, dailyDigestHourOfDay, dailyDigestNextRun, purgeDataFrequencyDays, purgeDataNextRun);
    }

    @Override
    public SchedulingConfig createEmptyGlobalRestModel() {
        return new SchedulingConfig();
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
