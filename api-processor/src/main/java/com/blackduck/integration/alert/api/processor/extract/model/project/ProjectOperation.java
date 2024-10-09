/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model.project;

import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;

public enum ProjectOperation {
    CREATE,
    DELETE;

    public static ProjectOperation fromOperationType(OperationType operationType) {
        return OperationType.DELETE.equals(operationType) ? ProjectOperation.DELETE : ProjectOperation.CREATE;
    }

}
