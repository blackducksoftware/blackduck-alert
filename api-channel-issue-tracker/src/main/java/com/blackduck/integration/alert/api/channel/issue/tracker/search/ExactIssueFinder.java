package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public interface ExactIssueFinder<T extends Serializable> {
    IssueTrackerSearchResult<T> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

}
