/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ProjectIssueSearchResult<T extends Serializable> extends AlertSerializableModel {
    private final ExistingIssueDetails<T> existingIssueDetails;
    private final ProjectIssueModel projectIssueModel;

    public ProjectIssueSearchResult(ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel projectIssueModel) {
        this.existingIssueDetails = existingIssueDetails;
        this.projectIssueModel = projectIssueModel;
    }

    public ExistingIssueDetails<T> getExistingIssueDetails() {
        return existingIssueDetails;
    }

    public ProjectIssueModel getProjectIssueModel() {
        return projectIssueModel;
    }
}
