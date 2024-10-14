/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
