/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem;

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
