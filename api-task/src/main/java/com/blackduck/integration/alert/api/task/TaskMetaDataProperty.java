package com.blackduck.integration.alert.api.task;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class TaskMetaDataProperty extends AlertSerializableModel {
    private static final long serialVersionUID = -295389156262597054L;
    private final String key;
    private final String displayName;
    private final String value;

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
