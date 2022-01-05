/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import com.synopsys.integration.blackduck.api.manual.enumeration.OperationType;

public enum ProjectOperation {
    CREATE,
    DELETE;

    public static ProjectOperation fromOperationType(OperationType operationType) {
        return OperationType.DELETE.equals(operationType) ? ProjectOperation.DELETE : ProjectOperation.CREATE;
    }

}
