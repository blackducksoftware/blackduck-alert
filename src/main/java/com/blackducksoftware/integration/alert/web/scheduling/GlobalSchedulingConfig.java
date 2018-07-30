/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.web.scheduling;

import com.blackducksoftware.integration.alert.web.model.Config;

public class GlobalSchedulingConfig extends Config {
    private String accumulatorNextRun;
    private String dailyDigestHourOfDay;
    private String dailyDigestNextRun;
    private String purgeDataFrequencyDays;
    private String purgeDataNextRun;

    public GlobalSchedulingConfig() {

    }

    public GlobalSchedulingConfig(final String id, final String accumulatorNextRun, final String dailyDigestHourOfDay, final String dailyDigestNextRun, final String purgeDataFrequencyDays, final String purgeDataNextRun) {
        super(id);
        this.accumulatorNextRun = accumulatorNextRun;
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
        this.dailyDigestNextRun = dailyDigestNextRun;
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
        this.purgeDataNextRun = purgeDataNextRun;
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

    public void setAccumulatorNextRun(final String accumulatorNextRun) {
        this.accumulatorNextRun = accumulatorNextRun;
    }

    public void setDailyDigestNextRun(final String dailyDigestNextRun) {
        this.dailyDigestNextRun = dailyDigestNextRun;
    }

    public void setPurgeDataNextRun(final String purgeDataNextRun) {
        this.purgeDataNextRun = purgeDataNextRun;
    }

    public void setDailyDigestHourOfDay(final String dailyDigestHourOfDay) {
        this.dailyDigestHourOfDay = dailyDigestHourOfDay;
    }

    public void setPurgeDataFrequencyDays(final String purgeDataFrequencyDays) {
        this.purgeDataFrequencyDays = purgeDataFrequencyDays;
    }

}
