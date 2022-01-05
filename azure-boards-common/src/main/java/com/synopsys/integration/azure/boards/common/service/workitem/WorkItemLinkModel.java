/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem;

public class WorkItemLinkModel {
    private String rel;
    private WorkItemReferenceModel source;
    private WorkItemReferenceModel target;

    public WorkItemLinkModel() {
        // For serialization
    }

    public WorkItemLinkModel(String rel, WorkItemReferenceModel source, WorkItemReferenceModel target) {
        this.rel = rel;
        this.source = source;
        this.target = target;
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
