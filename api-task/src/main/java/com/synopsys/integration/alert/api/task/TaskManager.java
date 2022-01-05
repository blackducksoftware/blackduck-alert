/*
 * api-task
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class TaskManager {
    private final Map<String, ScheduledTask> scheduledTaskMap = new HashMap<>();

    public final void registerTask(ScheduledTask scheduledTask) {
        if (scheduledTask != null) {
            scheduledTaskMap.put(scheduledTask.getTaskName(), scheduledTask);
        }
    }

    public final Optional<ScheduledTask> unregisterTask(String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        unScheduleTask(taskName);
        return Optional.of(scheduledTaskMap.remove(taskName));

    }

    public final int getTaskCount() {
        return scheduledTaskMap.size();
    }

    public final boolean scheduleCronTask(String cronExpression, String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecution(cronExpression);
        return true;
    }

    public final boolean scheduleExecutionAtFixedRate(long period, String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecutionAtFixedRate(period);
        return true;
    }

    public final boolean unScheduleTask(String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return false;
        }

        ScheduledTask task = scheduledTaskMap.get(taskName);
        task.scheduleExecution("");
        return true;
    }

    public Set<String> getRunningTaskNames() {
        return scheduledTaskMap.keySet();
    }

    public Collection<ScheduledTask> getRunningTasks() {
        return scheduledTaskMap.values();
    }

    /**
     * @param classOrSuperclass A class object that a ScheduledTask may be assignable to
     * @param <T>               A class that extends ScheduledTask
     * @return All tasks assignable to the 'classOrSuperclass' parameter
     */
    public <T extends ScheduledTask> List<T> getTasksByClass(Class<T> classOrSuperclass) {
        return scheduledTaskMap.values()
                   .stream()
                   .filter(task -> classOrSuperclass.isAssignableFrom(task.getClass()))
                   .map(classOrSuperclass::cast)
                   .collect(Collectors.toList());
    }

    public final Optional<String> getNextRunTime(String taskName) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        return scheduledTaskMap.get(taskName).getFormatedNextRunTime();
    }

    public final Optional<Long> getDifferenceToNextRun(String taskName, TimeUnit timeUnit) {
        if (!scheduledTaskMap.containsKey(taskName)) {
            return Optional.empty();
        }
        Optional<Long> millisecondsToNextRun = scheduledTaskMap.get(taskName).getMillisecondsToNextRun();
        return millisecondsToNextRun
                   .map(value -> timeUnit.convert(value, TimeUnit.MILLISECONDS));
    }

}
