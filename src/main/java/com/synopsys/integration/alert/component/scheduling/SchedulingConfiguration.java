package com.synopsys.integration.alert.component.scheduling;

import com.synopsys.integration.alert.common.configuration.Configuration;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;

public class SchedulingConfiguration extends Configuration {
    private String dailyDigestHourOfDay;
    private String dataFrequencyDays;

    public SchedulingConfiguration(ConfigurationModel configurationModel) {
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
