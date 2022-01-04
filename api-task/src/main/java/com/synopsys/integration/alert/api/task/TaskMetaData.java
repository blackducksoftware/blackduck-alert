/*
 * api-task
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.task;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class TaskMetaData extends AlertSerializableModel {
    private static final long serialVersionUID = -3249768131233749231L;
    private final String taskName;
    private final String type;
    private final String fullyQualifiedType;
    private final String nextRunTime;
    private final List<TaskMetaDataProperty> properties;

    public TaskMetaData(String taskName, String type, String fullyQualifiedType, String nextRunTime, List<TaskMetaDataProperty> properties) {
        this.taskName = taskName;
        this.type = type;
        this.fullyQualifiedType = fullyQualifiedType;
        this.nextRunTime = nextRunTime;
        this.properties = properties;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getType() {
        return type;
    }

    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }

    public String getNextRunTime() {
        return nextRunTime;
    }

    public List<TaskMetaDataProperty> getProperties() {
        return properties;
    }

}
