/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.ProviderConfigMissingValidator;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;

@Component
public class ProvidersMissingTask extends StartupScheduledTask {
    private ProviderConfigMissingValidator providerMissingValidator;

    @Autowired
    public ProvidersMissingTask(TaskScheduler taskScheduler, TaskManager taskManager, ProviderConfigMissingValidator providerMissingValidator) {
        super(taskScheduler, taskManager);
        this.providerMissingValidator = providerMissingValidator;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_HOUR_CRON_EXPRESSION;
    }

    @Override
    public void runTask() {
        providerMissingValidator.validate();
    }

    @Override
    protected void postTaskStartup() {
        runTask();
    }
}
