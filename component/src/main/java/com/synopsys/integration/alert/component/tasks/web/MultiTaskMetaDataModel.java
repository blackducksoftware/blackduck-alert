/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks.web;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.task.TaskMetaData;

public class MultiTaskMetaDataModel extends AlertSerializableModel {
    private final List<TaskMetaData> tasks;

    public MultiTaskMetaDataModel(List<TaskMetaData> tasks) {
        this.tasks = tasks;
    }

    public List<TaskMetaData> getTasks() {
        return tasks;
    }

}
