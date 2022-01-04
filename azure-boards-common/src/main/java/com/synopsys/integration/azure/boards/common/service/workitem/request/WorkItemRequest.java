/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem.request;

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
