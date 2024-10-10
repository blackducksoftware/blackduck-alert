/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.query;

import com.blackduck.integration.alert.azure.boards.common.service.workitem.WorkItemFieldReferenceModel;

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
