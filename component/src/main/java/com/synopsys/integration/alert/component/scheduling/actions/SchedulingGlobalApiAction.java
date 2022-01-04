/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.actions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.task.ScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.workflow.DailyTask;
import com.synopsys.integration.alert.component.scheduling.workflow.PurgeTask;

@Component
public class SchedulingGlobalApiAction extends ApiAction {
    private final TaskManager taskManager;

    @Autowired
    public SchedulingGlobalApiAction(TaskManager taskManager) {
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
        return calculateNextRuntime(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) {
        FieldModel updatedFieldModel = handleNewAndSavedConfig(currentFieldModel);
        updatedFieldModel = calculateNextRuntime(updatedFieldModel);

        return updatedFieldModel;
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

    private FieldModel calculateNextRuntime(FieldModel fieldModel) {
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(ScheduledTask.computeTaskName(DailyTask.class)).orElse("")), true));
        String processFrequency = fieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).orElse(String.valueOf(DailyTask.DEFAULT_HOUR_OF_DAY));
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of(processFrequency), true));

        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN, new FieldValueModel(List.of(taskManager.getNextRunTime(ScheduledTask.computeTaskName(PurgeTask.class)).orElse("")), true));
        String purgeFrequency = fieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).orElse(String.valueOf(PurgeTask.DEFAULT_FREQUENCY));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of(purgeFrequency), true));

        return fieldModel;
    }

}
