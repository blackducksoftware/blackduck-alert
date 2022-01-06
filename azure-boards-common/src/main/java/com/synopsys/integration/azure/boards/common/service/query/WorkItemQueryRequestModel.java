/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query;

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
