package com.synopsys.integration.azure.boards.common.service.workitem;

public class WorkItemLinkModel {
    private String rel;
    private WorkItemReferenceModel source;
    private WorkItemReferenceModel target;

    public WorkItemLinkModel() {
        // For serialization
    }

    public String getRel() {
        return rel;
    }

    public WorkItemReferenceModel getSource() {
        return source;
    }

    public WorkItemReferenceModel getTarget() {
        return target;
    }

}
