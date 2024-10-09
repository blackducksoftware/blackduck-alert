package com.blackduck.integration.alert.azure.boards.common.service.query.fluent;

import java.util.Optional;

public class WorkItemOrderByField {
    private final String fieldName;
    private WorkItemOrderByDirection direction;

    public WorkItemOrderByField(String fieldName) {
        this.fieldName = fieldName;
    }

    public WorkItemOrderByField(String fieldName, WorkItemOrderByDirection direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Optional<WorkItemOrderByDirection> getDirection() {
        return Optional.ofNullable(direction);
    }

}
