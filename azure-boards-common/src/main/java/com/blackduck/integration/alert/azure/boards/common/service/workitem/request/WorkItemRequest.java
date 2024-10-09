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
