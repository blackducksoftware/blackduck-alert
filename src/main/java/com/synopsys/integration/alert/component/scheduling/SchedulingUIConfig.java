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
package com.synopsys.integration.alert.component.scheduling;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class SchedulingUIConfig extends UIConfig {
    private static final String SCHEDULING_DIGEST_HOUR_DESCRIPTION = "Select the hour of the day to run the the daily digest distribution jobs.";
    private static final String SCHEDULING_PURGE_FREQUENCY_DESCRIPTION = "Choose a frequency for cleaning up provider data; the default value is three days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 3 days will be deleted.";

    public SchedulingUIConfig() {
        super(SchedulingDescriptor.SCHEDULING_LABEL, SchedulingDescriptor.SCHEDULING_DESCRIPTION, SchedulingDescriptor.SCHEDULING_URL, SchedulingDescriptor.SCHEDULING_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField digestHour = SelectConfigField.createRequired(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, "Daily digest hour of day", SCHEDULING_DIGEST_HOUR_DESCRIPTION,
            getRangeOfNumbers(0, 23));
        final ConfigField purgeFrequency = SelectConfigField.createRequired(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, "Purge data frequency in days", SCHEDULING_PURGE_FREQUENCY_DESCRIPTION,
            getRangeOfNumbers(1, 7));
        return Arrays.asList(digestHour, purgeFrequency);
    }

    private List<String> getRangeOfNumbers(final Integer minimumAllowedValue, final Integer maximumAllowedValue) {
        final List<String> numbers = new LinkedList<>();
        Integer counter = minimumAllowedValue;
        while (counter <= maximumAllowedValue) {
            numbers.add(String.valueOf(counter));
            counter++;
        }
        return numbers;
    }

}
