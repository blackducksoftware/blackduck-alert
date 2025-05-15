/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution;

import java.util.Collection;
import java.util.List;

import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldConfig;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraResolvedCustomField;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageValueReplacementResolver;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;

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
        MessageReplacementValues customFieldReplacementValues,
        Collection<JiraJobCustomFieldModel> customFields
    ) {
        List<JiraCustomFieldConfig> customFieldConfigs = customFields
                                                             .stream()
                                                             .map(customField -> new JiraCustomFieldConfig(customField.getFieldName(), customField.getFieldValue(), customField.isTreatValueAsJson()))
                                                             .toList();
        return createIssueRequestModel(summary, description, projectId, issueType, customFieldConfigs, customFieldReplacementValues);
    }

    public IssueRequestModelFieldsBuilder createIssueRequestModel(
        String summary,
        String description,
        String projectId,
        String issueType,
        Collection<JiraCustomFieldConfig> customFields,
        MessageReplacementValues customFieldReplacementValues
    ) {
        IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder()
                                                           .setSummary(summary)
                                                           .setDescription(description)
                                                           .setProject(projectId)
                                                           .setIssueType(issueType);
        for (JiraCustomFieldConfig customField : customFields) {
            MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(customFieldReplacementValues);
            String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(customField.getFieldOriginalValue());
            customField.setFieldReplacementValue(replacedFieldValue);
            JiraResolvedCustomField resolvedCustomField = jiraCustomFieldResolver.resolveCustomField(customField);
            fieldsBuilder.setValue(resolvedCustomField.getFieldId(), resolvedCustomField.getFieldValue());
        }
        return fieldsBuilder;
    }

}
