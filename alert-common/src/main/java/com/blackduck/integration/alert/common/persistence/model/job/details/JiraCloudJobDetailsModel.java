/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.List;
import java.util.UUID;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

public class JiraCloudJobDetailsModel extends DistributionJobDetailsModel {
    private final String issueCreatorEmail;
    private final String projectNameOrKey;
    private final String issueType;
    private final String resolveTransition;
    private final String reopenTransition;
    private final List<JiraJobCustomFieldModel> customFields;
    private final String issueSummary;

    public JiraCloudJobDetailsModel(
        UUID jobId,
        String issueCreatorEmail,
        String projectNameOrKey,
        String issueType,
        String resolveTransition,
        String reopenTransition,
        List<JiraJobCustomFieldModel> customFields,
        String issueSummary
    ) {
        super(ChannelKeys.JIRA_CLOUD, jobId);
        this.issueCreatorEmail = issueCreatorEmail;
        this.projectNameOrKey = projectNameOrKey;
        this.issueType = issueType;
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
        this.customFields = customFields;
        this.issueSummary = issueSummary;
    }

    public String getIssueCreatorEmail() {
        return issueCreatorEmail;
    }

    public String getProjectNameOrKey() {
        return projectNameOrKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getResolveTransition() {
        return resolveTransition;
    }

    public String getReopenTransition() {
        return reopenTransition;
    }

    public List<JiraJobCustomFieldModel> getCustomFields() {
        return customFields;
    }

    public String getIssueSummary() {
        return issueSummary;
    }
}
