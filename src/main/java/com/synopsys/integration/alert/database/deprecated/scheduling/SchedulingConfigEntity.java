package com.synopsys.integration.alert.database.deprecated.scheduling;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "global_scheduling_config")
public class SchedulingConfigEntity extends DatabaseEntity {
    @Column(name = "alert_digest_daily_hour_of_day")
    private String dailyDigestHourOfDay;

    @Column(name = "alert_purge_data_frequency_days")
    private String purgeDataFrequencyDays;

    public SchedulingConfigEntity() {

    }

    public SchedulingConfigEntity(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays) {
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
    }

    public String getDailyDigestHourOfDay() {
        return dailyDigestHourOfDay;
    }

    public String getPurgeDataFrequencyDays() {
        return purgeDataFrequencyDays;
    }

}