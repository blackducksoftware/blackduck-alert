package com.synopsys.integration.alert.common.rest.model;

public class RoleTask extends AlertSerializableModel {
    private final String taskName;

    public RoleTask(final String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }
}
