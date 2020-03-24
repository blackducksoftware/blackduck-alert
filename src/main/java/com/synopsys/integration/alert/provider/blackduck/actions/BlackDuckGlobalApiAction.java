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
package com.synopsys.integration.alert.provider.blackduck.actions;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckSystemValidator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckDataSyncTask;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private final BlackDuckProviderKey blackDuckProviderKey;
    private final BlackDuckSystemValidator blackDuckValidator;
    private final TaskManager taskManager;
    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public BlackDuckGlobalApiAction(BlackDuckProviderKey blackDuckProviderKey, BlackDuckSystemValidator blackDuckValidator, TaskManager taskManager, ProviderDataAccessor providerDataAccessor) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.blackDuckValidator = blackDuckValidator;
        this.taskManager = taskManager;
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig();
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig();
        return super.afterUpdateAction(fieldModel);
    }

    @Override
    public void afterDeleteAction(String descriptorName, String context) {
        taskManager.unScheduleTask(BlackDuckAccumulator.TASK_NAME);
        taskManager.unScheduleTask(BlackDuckDataSyncTask.TASK_NAME);

        List<ProviderProject> blackDuckProjects = providerDataAccessor.findByProviderKey(blackDuckProviderKey);
        providerDataAccessor.deleteProjects(blackDuckProviderKey, blackDuckProjects);
    }

    private void handleNewOrUpdatedConfig() {
        boolean valid = blackDuckValidator.validate();
        if (valid) {
            Optional<String> nextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
            if (nextRunTime.isEmpty()) {
                taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, BlackDuckAccumulator.TASK_NAME);
                taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, BlackDuckDataSyncTask.TASK_NAME);
            }
        }
    }

}
