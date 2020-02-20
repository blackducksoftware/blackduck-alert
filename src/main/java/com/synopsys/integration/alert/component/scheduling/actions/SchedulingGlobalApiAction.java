/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.scheduling.actions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderGlobalUIConfig;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;

@Component
// FIXME this class needs to be updated to handle multiple providers
public class SchedulingGlobalApiAction extends ApiAction {
    private BlackDuckProviderKey blackDuckProviderKey;
    private TaskManager taskManager;

    @Autowired
    public SchedulingGlobalApiAction(BlackDuckProviderKey blackDuckProviderKey, TaskManager taskManager) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.taskManager = taskManager;
    }

    @Override
    public FieldModel beforeSaveAction(FieldModel fieldModel) {
        return handleNewAndSavedConfig(fieldModel);
    }

    @Override
    public FieldModel beforeUpdateAction(FieldModel fieldModel) {
        return handleNewAndSavedConfig(fieldModel);
    }

    @Override
    public FieldModel afterGetAction(FieldModel fieldModel) {
        Map<String, FieldValueModel> keyToValues = fieldModel.getKeyToValues();
        String blackDuckGlobalConfigName = keyToValues.get(ProviderGlobalUIConfig.KEY_PROVIDER_CONFIG_NAME)
                                               .getValue()
                                               .orElseThrow();

        // FIXME do this dynamically:
        String accumulatorTaskName = ProviderTask.computeProviderTaskName(blackDuckProviderKey, blackDuckGlobalConfigName, BlackDuckAccumulator.class);
        String blackDuckNextRun = taskManager.getDifferenceToNextRun(accumulatorTaskName, TimeUnit.SECONDS).map(String::valueOf).orElse("");
        fieldModel.putField(SchedulingDescriptor.KEY_BLACKDUCK_NEXT_RUN, new FieldValueModel(List.of(blackDuckNextRun), true));
        // end of block to clean up

        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(ScheduledTask.computeTaskName(DailyTask.class)).orElse("")), true));
        String processFrequency = fieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).orElse(String.valueOf(DailyTask.DEFAULT_HOUR_OF_DAY));
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of(processFrequency), true));

        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(ScheduledTask.computeTaskName(PurgeTask.class)).orElse("")), true));
        String purgeFrequency = fieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).orElse(String.valueOf(PurgeTask.DEFAULT_FREQUENCY));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of(purgeFrequency), true));

        return fieldModel;
    }

    public FieldModel handleNewAndSavedConfig(FieldModel fieldModel) {
        String dailyDigestHourOfDay = fieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).orElse("");
        String purgeDataFrequencyDays = fieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).orElse("");
        String dailyDigestCron = String.format(DailyTask.CRON_FORMAT, dailyDigestHourOfDay);
        String purgeDataCron = String.format(PurgeTask.CRON_FORMAT, purgeDataFrequencyDays);
        taskManager.scheduleCronTask(dailyDigestCron, ScheduledTask.computeTaskName(DailyTask.class));
        taskManager.scheduleCronTask(purgeDataCron, ScheduledTask.computeTaskName(PurgeTask.class));
        return fieldModel;
    }

}
