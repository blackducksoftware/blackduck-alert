package com.blackduck.integration.alert.component.tasks.web;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.task.TaskMetaData;

public class MultiTaskMetaDataModel extends AlertSerializableModel {
    private final List<TaskMetaData> tasks;

    public MultiTaskMetaDataModel(List<TaskMetaData> tasks) {
        this.tasks = tasks;
    }

    public List<TaskMetaData> getTasks() {
        return tasks;
    }

}
