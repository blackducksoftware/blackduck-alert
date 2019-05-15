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
package com.synopsys.integration.alert.workflow.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertConstants;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SettingsKeyAccessor;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.workflow.update.model.UpdateModel;

@Component
public class UpdateNotifierTask extends ScheduledTask {
    public static final String TASK_NAME = "updatenotifier";
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";
    public static final String SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL = "update.email.sent.for.version";

    private final UpdateChecker updateChecker;
    private final SystemMessageUtility systemMessageUtility;
    private final SettingsKeyAccessor settingsKeyAccessor;

    @Autowired
    public UpdateNotifierTask(final TaskScheduler taskScheduler, final UpdateChecker updateChecker, final SystemMessageUtility systemMessageUtility, final SettingsKeyAccessor settingsKeyAccessor) {
        super(taskScheduler, TASK_NAME);
        this.updateChecker = updateChecker;
        this.systemMessageUtility = systemMessageUtility;
        this.settingsKeyAccessor = settingsKeyAccessor;
    }

    @Override
    public void runTask() {
        final UpdateModel updateModel = updateChecker.getUpdateModel();
        if (updateModel.isUpdatable()) {
            addSystemMessage(updateModel);
            sendUpdateEmail(updateModel);
        }
    }

    private void addSystemMessage(final UpdateModel updateModel) {
        final String message = String.format("There is a new version of %s available: %s", AlertConstants.ALERT_APPLICATION_NAME, updateModel.getLatestAvailableVersion());
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.UPDATE_AVAILABLE);
        systemMessageUtility.addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.UPDATE_AVAILABLE);
    }

    private void sendUpdateEmail(final UpdateModel updateModel) {
        // FIXME implement
        //  final Optional<SettingsKeyModel> optionalSetting = settingsKeyAccessor.getSettingsKeyByKey(SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL);
    }

}
