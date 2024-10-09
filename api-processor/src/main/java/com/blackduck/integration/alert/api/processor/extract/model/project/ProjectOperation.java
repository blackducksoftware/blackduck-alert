package com.blackduck.integration.alert.api.processor.extract.model.project;

import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;

public enum ProjectOperation {
    CREATE,
    DELETE;

    public static ProjectOperation fromOperationType(OperationType operationType) {
        return OperationType.DELETE.equals(operationType) ? ProjectOperation.DELETE : ProjectOperation.CREATE;
    }

}
