package com.blackduck.integration.alert.azure.boards.common.service.query;

public class WorkItemQueryRequestModel {
    private String query;

    public WorkItemQueryRequestModel() {
        // For serialization
    }

    public WorkItemQueryRequestModel(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

}
