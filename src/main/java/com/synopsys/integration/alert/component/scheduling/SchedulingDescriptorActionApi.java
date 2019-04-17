/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.action.NoTestActionApi;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;

@Component
public class SchedulingDescriptorActionApi extends NoTestActionApi {
    private final TaskManager taskManager;

    @Autowired
    public SchedulingDescriptorActionApi(final TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public FieldModel readConfig(final FieldModel fieldModel) {
        final String blackDuckNextRun = taskManager.getDifferenceToNextRun(BlackDuckAccumulator.TASK_NAME, TimeUnit.SECONDS).map(String::valueOf).orElse("");
        final String polarisNextRun = taskManager.getDifferenceToNextRun(PolarisProjectSyncTask.TASK_NAME, TimeUnit.SECONDS).map(String::valueOf).orElse("");

        fieldModel.putField(SchedulingDescriptor.KEY_BLACKDUCK_NEXT_RUN, new FieldValueModel(List.of(blackDuckNextRun), true));
        fieldModel.putField(SchedulingDescriptor.KEY_POLARIS_NEXT_RUN, new FieldValueModel(List.of(polarisNextRun), true));
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(DailyTask.TASK_NAME).orElse("")), true));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(PurgeTask.TASK_NAME).orElse("")), true));
        return fieldModel;
    }

    @Override
    public FieldModel beforeUpdateConfig(final FieldModel fieldModel) {
        return beforeSaveConfig(fieldModel);
    }

    @Override
    public FieldModel beforeSaveConfig(final FieldModel fieldModel) {
        final String dailyDigestHourOfDay = fieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).orElse("");
        final String purgeDataFrequencyDays = fieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).orElse("");
        final String dailyDigestCron = String.format(DailyTask.CRON_FORMAT, dailyDigestHourOfDay);
        final String purgeDataCron = String.format(PurgeTask.CRON_FORMAT, purgeDataFrequencyDays);
        taskManager.scheduleCronTask(dailyDigestCron, DailyTask.TASK_NAME);
        taskManager.scheduleCronTask(purgeDataCron, PurgeTask.TASK_NAME);
        return fieldModel;
    }

}
