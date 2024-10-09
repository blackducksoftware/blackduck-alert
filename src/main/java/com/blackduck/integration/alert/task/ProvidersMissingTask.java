package com.blackduck.integration.alert.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.provider.ProviderConfigMissingValidator;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.StartupScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;

@Component
public class ProvidersMissingTask extends StartupScheduledTask {
    private final ProviderConfigMissingValidator providerMissingValidator;

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
