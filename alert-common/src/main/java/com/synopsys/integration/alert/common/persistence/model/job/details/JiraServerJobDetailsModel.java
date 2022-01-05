/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class JiraServerJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String issueCreatorUsername;
    private final String projectNameOrKey;
    private final String issueType;
    private final String resolveTransition;
    private final String reopenTransition;
    private final List<JiraJobCustomFieldModel> customFields;
    private final String issueSummary;

    public JiraServerJobDetailsModel(
        UUID jobId,
        boolean addComments,
        String issueCreatorUsername,
        String projectNameOrKey,
        String issueType,
        String resolveTransition,
        String reopenTransition,
        List<JiraJobCustomFieldModel> customFields,
        String issueSummary
    ) {
        super(ChannelKeys.JIRA_SERVER, jobId);
        this.addComments = addComments;
        this.issueCreatorUsername = issueCreatorUsername;
        this.projectNameOrKey = projectNameOrKey;
        this.issueType = issueType;
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
        this.customFields = customFields;
        this.issueSummary = issueSummary;
    }

    public boolean isAddComments() {
        return addComments;
    }

    public String getIssueCreatorUsername() {
        return issueCreatorUsername;
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
