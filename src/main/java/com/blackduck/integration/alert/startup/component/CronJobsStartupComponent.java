package com.blackduck.integration.alert.startup.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.task.StartupScheduledTask;

@Component
@Order(50)
public class CronJobsStartupComponent extends StartupComponent {
    private final List<StartupScheduledTask> startupScheduledTasks;

    @Autowired
    public CronJobsStartupComponent(List<StartupScheduledTask> startupScheduledTasks) {
        this.startupScheduledTasks = startupScheduledTasks;
    }

    @Override
    protected void initialize() {
        startupScheduledTasks.forEach(StartupScheduledTask::startTask);
    }

}
