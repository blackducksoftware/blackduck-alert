/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
