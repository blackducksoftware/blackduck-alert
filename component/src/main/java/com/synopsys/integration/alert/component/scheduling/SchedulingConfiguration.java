/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.scheduling;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.Configuration;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;

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
