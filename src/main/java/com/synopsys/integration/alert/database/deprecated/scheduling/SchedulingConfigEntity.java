/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.deprecated.scheduling;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Deprecated(since = "4.0.0; removed in 6.0.0", forRemoval = true)
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