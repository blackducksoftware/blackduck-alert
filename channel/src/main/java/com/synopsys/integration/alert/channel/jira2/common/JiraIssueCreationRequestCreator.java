/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.jira2.common;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.channel.jira2.common.model.JiraResolvedCustomField;
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
            JiraCustomFieldValueReplacementUtils.injectReplacementFieldValue(customField, customFieldReplacementValues);
            JiraResolvedCustomField resolvedCustomField = jiraCustomFieldResolver.resolveCustomField(customField);
            fieldsBuilder.setValue(resolvedCustomField.getFieldId(), resolvedCustomField.getFieldValue());
        }
        return fieldsBuilder;
    }

}
