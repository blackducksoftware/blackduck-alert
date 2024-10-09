package com.blackduck.integration.alert.azure.boards.common.service.workitem;

public class WorkItemFieldReferenceModel {
    private String name;
    private String referenceName;
    private String url;

    public WorkItemFieldReferenceModel() {
        // For serialization
    }

    public WorkItemFieldReferenceModel(String name, String referenceName, String url) {
        this.name = name;
        this.referenceName = referenceName;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getUrl() {
        return url;
    }

}
