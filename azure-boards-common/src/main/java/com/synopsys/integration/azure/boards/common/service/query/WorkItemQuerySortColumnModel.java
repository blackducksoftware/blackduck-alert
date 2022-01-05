/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query;

import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemFieldReferenceModel;

public class WorkItemQuerySortColumnModel {
    private Boolean descending;
    private WorkItemFieldReferenceModel field;

    public WorkItemQuerySortColumnModel() {
        // For serialization
    }

    public Boolean getDescending() {
        return descending;
    }

    public WorkItemFieldReferenceModel getField() {
        return field;
    }

}
