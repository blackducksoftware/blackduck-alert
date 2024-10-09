/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.type;

import java.util.List;

public class WorkItemTypeTransitionModel {
    private String to;
    private List<String> actions;

    public WorkItemTypeTransitionModel() {
        // For serialization
    }

    public WorkItemTypeTransitionModel(String to, List<String> actions) {
        this.to = to;
        this.actions = actions;
    }

    public String getTo() {
        return to;
    }

    public List<String> getActions() {
        return actions;
    }

}
