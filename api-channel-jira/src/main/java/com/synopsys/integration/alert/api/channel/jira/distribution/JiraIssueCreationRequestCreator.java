/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldConfig;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldValueReplacementResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraResolvedCustomField;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;

public class JiraIssueCreationRequestCreator {
    private final JiraCustomFieldResolver jiraCustomFieldResolver;

    public JiraIssueCreationRequestCreator(JiraCustomFieldResolver jiraCustomFieldResolver) {
        this.jiraCustomFieldResolver = jiraCustomFieldResolver;
    }

    public IssueRequestModelFieldsBuilder createIssueRequestModel(
        String summary,
        String description,
        String projectId,
        String issueType,
        JiraCustomFieldReplacementValues customFieldReplacementValues,
        Collection<JiraJobCustomFieldModel> customFields
    ) {
        List<JiraCustomFieldConfig> customFieldConfigs = customFields
                                                             .stream()
                                                             .map(customField -> new JiraCustomFieldConfig(customField.getFieldName(), customField.getFieldValue()))
                                                             .collect(Collectors.toList());
        return createIssueRequestModel(summary, description, projectId, issueType, customFieldConfigs, customFieldReplacementValues);
    }

    public IssueRequestModelFieldsBuilder createIssueRequestModel(
        String summary,
        String description,
        String projectId,
        String issueType,
        Collection<JiraCustomFieldConfig> customFields,
        JiraCustomFieldReplacementValues customFieldReplacementValues
    ) {
        IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder()
                                                           .setSummary(summary)
                                                           .setDescription(description)
                                                           .setProject(projectId)
                                                           .setIssueType(issueType);
        for (JiraCustomFieldConfig customField : customFields) {
            JiraCustomFieldValueReplacementResolver jiraCustomFieldValueReplacementResolver = new JiraCustomFieldValueReplacementResolver(customFieldReplacementValues);
            jiraCustomFieldValueReplacementResolver.injectReplacementFieldValue(customField);
            JiraResolvedCustomField resolvedCustomField = jiraCustomFieldResolver.resolveCustomField(customField);
            fieldsBuilder.setValue(resolvedCustomField.getFieldId(), resolvedCustomField.getFieldValue());
        }
        return fieldsBuilder;
    }

}
