package com.synopsys.integration.alert.web.tasks;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.web.model.TaskModel;

@Component
public class TaskActions {
    private TaskManager taskManager;

    @Autowired
    public TaskActions(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public Collection<TaskModel> getTasks() {
        Collection<ScheduledTask> tasks = taskManager.getRunningTasks();
        return tasks.stream()
                   .map(task -> new TaskModel(task.getTaskName(), task.getFormatedNextRunTime().orElse("")))
                   .sorted(Comparator.comparing(TaskModel::getName))
                   .collect(Collectors.toList());
    }
}
