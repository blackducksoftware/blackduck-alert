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
package com.blackducksoftware.integration.hub.alert.scheduling.mock;

import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.google.gson.JsonObject;

public class MockGlobalSchedulingEntity extends MockGlobalEntityUtil<GlobalSchedulingConfigEntity> {
    private final String dailyDigestHourOfDay;
    private final String purgeDataFrequencyDays;
    private final Long id;

    public MockGlobalSchedulingEntity() {
        this("2", "5", 1L);
    }

    private MockGlobalSchedulingEntity(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays, final Long id) {
        super();
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
        this.id = id;
    }

    public String getDailyDigestHourOfDay() {
        return dailyDigestHourOfDay;
    }

    public String getPurgeDataFrequencyDays() {
        return purgeDataFrequencyDays;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public GlobalSchedulingConfigEntity createGlobalEntity() {
        final GlobalSchedulingConfigEntity entity = new GlobalSchedulingConfigEntity(dailyDigestHourOfDay, purgeDataFrequencyDays);
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
        json.addProperty("dailyDigestHourOfDay", dailyDigestHourOfDay);
        json.addProperty("purgeDataFrequencyDays", purgeDataFrequencyDays);
        json.addProperty("id", id);
        return json.toString();
    }

}
