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
package com.synopsys.integration.alert.provider.blackduck.actions;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckProjectSyncTask;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private final SystemValidator systemValidator;
    private final TaskManager taskManager;

    @Autowired
    public BlackDuckGlobalApiAction(final SystemValidator systemValidator, final TaskManager taskManager) {
        this.systemValidator = systemValidator;
        this.taskManager = taskManager;
    }

    @Override
    public FieldModel afterSaveAction(final FieldModel fieldModel) {
        handleNewOrUpdatedConfig();
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(final FieldModel fieldModel) {
        handleNewOrUpdatedConfig();
        return super.afterUpdateAction(fieldModel);
    }

    @Override
    public void afterDeleteAction(final String descriptorName, final String context) {
        taskManager.unScheduleTask(BlackDuckAccumulator.TASK_NAME);
        taskManager.unScheduleTask(BlackDuckProjectSyncTask.TASK_NAME);
    }

    public void handleNewOrUpdatedConfig() {
        final boolean valid = systemValidator.validate();
        // This doesn't need to validate the whole system, just the Black Duck settings.
        if (valid) {
            final Optional<String> nextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
            if (nextRunTime.isEmpty()) {
                taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, BlackDuckAccumulator.TASK_NAME);
                taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, BlackDuckProjectSyncTask.TASK_NAME);
            }
        }
    }

}
