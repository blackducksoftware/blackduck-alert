package com.synopsys.integration.alert.component.scheduling;

import com.synopsys.integration.alert.common.configuration.Configuration;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;

public class SchedulingConfiguration extends Configuration {
    private final String dailyDigestHourOfDay;
    private final String dataFrequencyDays;

    public SchedulingConfiguration(final ConfigurationModel configurationModel) {
        super(configurationModel);

        dailyDigestHourOfDay = getFieldAccessor().getString(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY);
        dataFrequencyDays = getFieldAccessor().getString(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS);
    }

    public String getDailyDigestHourOfDay() {
        return dailyDigestHourOfDay;
    }

    public String getDataFrequencyDays() {
        return dataFrequencyDays;
    }
}
