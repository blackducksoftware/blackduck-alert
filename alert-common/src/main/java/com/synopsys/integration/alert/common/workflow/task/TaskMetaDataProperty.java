package com.synopsys.integration.alert.common.workflow.task;

public class TaskMetaDataProperty {
    private String key;
    private String displayName;
    private String value;

    public TaskMetaDataProperty(String key, String displayName, String value) {
        this.key = key;
        this.displayName = displayName;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValue() {
        return value;
    }
}
