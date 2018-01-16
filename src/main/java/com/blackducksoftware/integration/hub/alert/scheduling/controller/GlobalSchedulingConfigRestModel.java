/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class GlobalSchedulingConfigRestModel extends ConfigRestModel {
    private String accumulatorNextRun;
    private String dailyDigestCron;
    private String dailyDigestNextRun;
    private String purgeDataCron;
    private String purgeDataNextRun;

    public GlobalSchedulingConfigRestModel() {

    }

    public GlobalSchedulingConfigRestModel(final String id, final String accumulatorNextRun, final String dailyDigestCron, final String dailyDigestNextRun, final String purgeDataCron, final String purgeDataNextRun) {
        super(id);
        this.accumulatorNextRun = accumulatorNextRun;
        this.dailyDigestCron = dailyDigestCron;
        this.dailyDigestNextRun = dailyDigestNextRun;
        this.purgeDataCron = purgeDataCron;
        this.purgeDataNextRun = purgeDataNextRun;
    }

    public String getAccumulatorNextRun() {
        return accumulatorNextRun;
    }

    public String getDailyDigestCron() {
        return dailyDigestCron;
    }

    public String getDailyDigestNextRun() {
        return dailyDigestNextRun;
    }

    public String getPurgeDataCron() {
        return purgeDataCron;
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

}
