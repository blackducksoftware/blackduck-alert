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
package com.synopsys.integration.alert.workflow.scheduled.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertConstants;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;

@Component
public class UpdateNotifierTask extends StartupScheduledTask {
    public static final String TASK_NAME = "updatenotifier";
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";

    private final UpdateChecker updateChecker;
    private final SystemMessageAccessor systemMessageAccessor;
    private final UpdateEmailService updateEmailService;

    @Autowired
    public UpdateNotifierTask(TaskScheduler taskScheduler, UpdateChecker updateChecker, SystemMessageAccessor systemMessageAccessor, UpdateEmailService updateEmailService, TaskManager taskManager) {
        super(taskScheduler, taskManager);
        this.updateChecker = updateChecker;
        this.systemMessageAccessor = systemMessageAccessor;
        this.updateEmailService = updateEmailService;
    }

    @Override
    public void runTask() {
        UpdateModel updateModel = updateChecker.getUpdateModel();
        if (updateModel.getUpdatable()) {
            addSystemMessage(updateModel.getDockerTagVersion());
            updateEmailService.sendUpdateEmail(updateModel);
        }
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    private void addSystemMessage(String versionName) {
        String message = String.format("There is a new version of %s available: %s", AlertConstants.ALERT_APPLICATION_NAME, versionName);
        systemMessageAccessor.removeSystemMessagesByType(SystemMessageType.UPDATE_AVAILABLE);
        systemMessageAccessor.addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.UPDATE_AVAILABLE);
    }

}
