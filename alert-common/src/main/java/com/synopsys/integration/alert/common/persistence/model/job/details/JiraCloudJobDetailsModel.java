/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.List;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class JiraCloudJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String issueCreatorEmail;
    private final String projectNameOrKey;
    private final String issueType;
    private final String resolveTransition;
    private final String reopenTransition;
    private final List<JiraJobCustomFieldModel> customFields;

    public JiraCloudJobDetailsModel(
        boolean addComments,
        String issueCreatorEmail,
        String projectNameOrKey,
        String issueType,
        String resolveTransition,
        String reopenTransition,
        List<JiraJobCustomFieldModel> customFields
    ) {
        super(ChannelKey.JIRA_CLOUD);
        this.addComments = addComments;
        this.issueCreatorEmail = issueCreatorEmail;
        this.projectNameOrKey = projectNameOrKey;
        this.issueType = issueType;
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
        this.customFields = customFields;
    }

    public boolean isAddComments() {
        return addComments;
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

}
