/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.model;

import java.util.List;

import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;

public class JiraIssueConfig extends IssueConfig {
    private final List<JiraCustomFieldConfig> customFields;

    public JiraIssueConfig(String projectName, String projectKey, String projectId, String issueCreator, String issueType, boolean commentOnIssues, String resolveTransition, String openTransition, List<JiraCustomFieldConfig> customFields) {
        super(projectName, projectKey, projectId, issueCreator, issueType, commentOnIssues, resolveTransition, openTransition);
        this.customFields = customFields;
    }

    public List<JiraCustomFieldConfig> getCustomFields() {
        return customFields;
    }

}
