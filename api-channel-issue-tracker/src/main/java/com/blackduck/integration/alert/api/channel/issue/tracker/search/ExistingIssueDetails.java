package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ExistingIssueDetails<T extends Serializable> extends AlertSerializableModel {
    private static final long serialVersionUID = 4683457270197617111L;
    private final T issueId;
    private final String issueKey;
    private final String issueSummary;
    private final String issueUILink;
    private final IssueStatus issueStatus;
    private final IssueCategory issueCategory;

    public ExistingIssueDetails(T issueId, String issueKey, String issueSummary, String issueUILink, IssueStatus issueStatus, IssueCategory issueCategory) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.issueSummary = issueSummary;
        this.issueUILink = issueUILink;
        this.issueStatus = issueStatus;
        this.issueCategory = issueCategory;
    }

    public T getIssueId() {
        return issueId;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getIssueUILink() {
        return issueUILink;
    }

    public IssueStatus getIssueStatus() {
        return issueStatus;
    }

    public IssueCategory getIssueCategory() {
        return issueCategory;
    }

}
