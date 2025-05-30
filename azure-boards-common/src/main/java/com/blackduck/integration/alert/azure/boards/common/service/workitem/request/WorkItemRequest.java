/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.workitem.request;

import java.util.List;

public class WorkItemRequest {
    private final List<WorkItemElementOperationModel> elementOperationModels;

    public WorkItemRequest(List<WorkItemElementOperationModel> elementOperationModels) {
        this.elementOperationModels = elementOperationModels;
    }

    public List<WorkItemElementOperationModel> getElementOperationModels() {
        return elementOperationModels;
    }

}
