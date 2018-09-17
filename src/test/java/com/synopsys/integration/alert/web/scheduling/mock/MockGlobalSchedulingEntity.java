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
package com.synopsys.integration.alert.web.scheduling.mock;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.mock.MockGlobalEntityUtil;
import com.synopsys.integration.alert.web.component.scheduling.SchedulingConfig;
import com.synopsys.integration.alert.web.model.Config;

public class MockGlobalSchedulingEntity extends MockGlobalEntityUtil<SchedulingConfigEntity> {
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
    public Config createGlobalConfig() {
        return new SchedulingConfig(id.toString(), final String accumulatorNextRun, final String dailyDigestHourOfDay, final String dailyDigestNextRun, final String purgeDataFrequencyDays, final String purgeDataNextRun);
    }

    @Override
    public Config createEmptyGlobalConfig() {
        return new SchedulingConfig();
    }

    @Override
    public SchedulingConfigEntity createGlobalEntity() {
        final SchedulingConfigEntity entity = new SchedulingConfigEntity(dailyDigestHourOfDay, purgeDataFrequencyDays);
        entity.setId(id);
        return entity;
    }

    @Override
    public SchedulingConfigEntity createEmptyGlobalEntity() {
        return new SchedulingConfigEntity();
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
