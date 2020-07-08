package com.synopsys.integration.azure.boards.common.service.workitem;

public class WorkItemReferenceModel {
    private Integer id;
    private String url;

    public WorkItemReferenceModel() {
        // For serialization
    }

    public WorkItemReferenceModel(Integer id, String url) {
        this.id = id;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

}
