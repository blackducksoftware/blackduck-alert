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
package com.synopsys.integration.alert.component.scheduling.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.ReadOnlyConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class SchedulingUIConfig extends UIConfig {
    private static final String LABEL_DAILY_DIGEST_HOUR_OF_DAY = "Daily Digest Hour Of Day";
    private static final String LABEL_DAILY_PROCESSOR_NEXT_RUN = "Daily Digest Cron Next Run";
    private static final String LABEL_PURGE_DATA_FREQUENCY_IN_DAYS = "Purge Data Frequency In Days";
    private static final String LABEL_PURGE_DATA_NEXT_RUN = "Purge Cron Next Run";

    private static final String SCHEDULING_DIGEST_HOUR_DESCRIPTION = "Select the hour of the day to run the daily digest distribution jobs.";
    private static final String DAILY_PROCESSOR_NEXT_RUN_DESCRIPTION = "This is the next time daily digest distribution jobs will run.";
    private static final String SCHEDULING_PURGE_FREQUENCY_DESCRIPTION = "Choose a frequency for cleaning up provider data; the default value is three days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 3 days will be deleted.";
    private static final String PURGE_DATA_NEXT_RUN_DESCRIPTION = "This is the next time Alert will purge provider data.";

    public SchedulingUIConfig() {
        super(SchedulingDescriptor.SCHEDULING_LABEL, SchedulingDescriptor.SCHEDULING_DESCRIPTION, SchedulingDescriptor.SCHEDULING_URL);
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField digestHour = new SelectConfigField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, LABEL_DAILY_DIGEST_HOUR_OF_DAY, SCHEDULING_DIGEST_HOUR_DESCRIPTION, createDigestHours()).applyRequired(true);
        ConfigField digestHourNextRun = new ReadOnlyConfigField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN, LABEL_DAILY_PROCESSOR_NEXT_RUN, DAILY_PROCESSOR_NEXT_RUN_DESCRIPTION);
        ConfigField purgeFrequency = new SelectConfigField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, LABEL_PURGE_DATA_FREQUENCY_IN_DAYS, SCHEDULING_PURGE_FREQUENCY_DESCRIPTION, createPurgeFrequency()).applyRequired(true);
        ConfigField purgeNextRun = new ReadOnlyConfigField(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN, LABEL_PURGE_DATA_NEXT_RUN, PURGE_DATA_NEXT_RUN_DESCRIPTION);
        return List.of(digestHour, digestHourNextRun, purgeFrequency, purgeNextRun);
    }

    private List<LabelValueSelectOption> createDigestHours() {
        return List.of(
            new LabelValueSelectOption("12 am", "0"),
            new LabelValueSelectOption("1 am", "1"),
            new LabelValueSelectOption("2 am", "2"),
            new LabelValueSelectOption("3 am", "3"),
            new LabelValueSelectOption("4 am", "4"),
            new LabelValueSelectOption("5 am", "5"),
            new LabelValueSelectOption("6 am", "6"),
            new LabelValueSelectOption("7 am", "7"),
            new LabelValueSelectOption("8 am", "8"),
            new LabelValueSelectOption("9 am", "9"),
            new LabelValueSelectOption("10 am", "10"),
            new LabelValueSelectOption("11 am", "11"),
            new LabelValueSelectOption("12 pm", "12"),
            new LabelValueSelectOption("1 pm", "13"),
            new LabelValueSelectOption("2 pm", "14"),
            new LabelValueSelectOption("3 pm", "15"),
            new LabelValueSelectOption("4 pm", "16"),
            new LabelValueSelectOption("5 pm", "17"),
            new LabelValueSelectOption("6 pm", "18"),
            new LabelValueSelectOption("7 pm", "19"),
            new LabelValueSelectOption("8 pm", "20"),
            new LabelValueSelectOption("9 pm", "21"),
            new LabelValueSelectOption("10 pm", "22"),
            new LabelValueSelectOption("11 pm", "23"));
    }

    private List<LabelValueSelectOption> createPurgeFrequency() {
        return List.of(
            new LabelValueSelectOption("Every day", "1"),
            new LabelValueSelectOption("Every 2 days", "2"),
            new LabelValueSelectOption("Every 3 days", "3"),
            new LabelValueSelectOption("Every 4 days", "4"),
            new LabelValueSelectOption("Every 5 days", "5"),
            new LabelValueSelectOption("Every 6 days", "6"),
            new LabelValueSelectOption("Every 7 days", "7"));
    }

}
