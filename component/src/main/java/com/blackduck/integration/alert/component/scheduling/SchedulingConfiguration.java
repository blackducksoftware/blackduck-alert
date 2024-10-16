/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling;

import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.Configuration;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;

public class SchedulingConfiguration extends Configuration {
    private final String dailyDigestHourOfDay;
    private final String dataFrequencyDays;

    public SchedulingConfiguration(ConfigurationModel configurationModel) {
        super(configurationModel.getCopyOfKeyToFieldMap());

        dailyDigestHourOfDay = getFieldUtility().getStringOrNull(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
        dataFrequencyDays = getFieldUtility().getStringOrNull(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
    }

    public String getDailyDigestHourOfDay() {
        return dailyDigestHourOfDay;
    }

    public String getDataFrequencyDays() {
        return dataFrequencyDays;
    }
}
