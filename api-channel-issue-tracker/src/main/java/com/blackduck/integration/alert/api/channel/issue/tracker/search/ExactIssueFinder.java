/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public interface ExactIssueFinder<T extends Serializable> {
    IssueTrackerSearchResult<T> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

}
