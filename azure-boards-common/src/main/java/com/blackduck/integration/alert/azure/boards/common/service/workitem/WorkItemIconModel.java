package com.blackduck.integration.alert.azure.boards.common.service.workitem;

public class WorkItemIconModel {
    private String id;
    private String url;

    public WorkItemIconModel() {
        // For serialization
    }

    public WorkItemIconModel(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

}
