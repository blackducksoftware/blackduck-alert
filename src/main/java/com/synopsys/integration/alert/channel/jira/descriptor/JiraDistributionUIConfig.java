/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.descriptor;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.JiraChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;

@Component
public class JiraDistributionUIConfig extends ChannelDistributionUIConfig {

    public static final String LABEL_ADD_COMMENTS = "Add Comments";
    public static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    public static final String LABEL_JIRA_PROJECT = "Jira Project";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    public static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";
    public static final String LABEL_RESOLVE_WORKFLOW_STATUS = "Resolve Status";
    public static final String LABEL_OPEN_WORKFLOW_STATUS = "Re-open Status";

    public static final String DESCRIPTION_ADD_COMMENTS = "If true, this will add comments to the Jira ticket with data describing the latest change.";
    public static final String DESCRIPTION_ISSUE_CREATOR = "The email of the Jira Cloud user to assign as the issue creator field of the Jira issue.";
    public static final String DESCRIPTION_JIRA_PROJECT = "The name or key of the Jira Project for which this job creates and/or updates Jira tickets.";
    public static final String DESCRIPTION_ISSUE_TYPE = "The issue type to open when creating an issue in Jira Cloud.";
    public static final String DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when resolving an issue. This will happen when Alert receives a DELETE operation from a provider.";
    public static final String DESCRIPTION_OPEN_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when re-opening an issue. This will happen when Alert receives an ADD/UPDATE operation from a provider.";
    public static final String DESCRIPTION_RESOLVE_WORKFLOW_STATUS = "This should be the expected status that the Resolve Transition transitions to. This way Alert can check if a transition should be done before attempting to do so and failing. This is required if the Resolve Transition field is set.";
    public static final String DESCRIPTION_OPEN_WORKFLOW_STATUS = "This should be the expected status that the Re-open Transition transitions to. This way Alert can check if a transition should be done before attempting to do so and failing. This is required if the Re-open Transition field is set.";

    public static final String DEFAULT_ISSUE_TYPE = "Task";

    @Autowired
    public JiraDistributionUIConfig(@Lazy final DescriptorMap descriptorMap) {
        super(JiraChannel.COMPONENT_NAME, JiraDescriptor.JIRA_LABEL, JiraDescriptor.JIRA_URL, JiraDescriptor.JIRA_ICON, descriptorMap);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField addComments = CheckboxConfigField.create(JiraDescriptor.KEY_ADD_COMMENTS, LABEL_ADD_COMMENTS, DESCRIPTION_ADD_COMMENTS);
        final ConfigField issueCreator = TextInputConfigField.create(JiraDescriptor.KEY_ISSUE_CREATOR, LABEL_ISSUE_CREATOR, DESCRIPTION_ISSUE_CREATOR);
        final ConfigField jiraProjectName = TextInputConfigField.createRequired(JiraDescriptor.KEY_JIRA_PROJECT_NAME, LABEL_JIRA_PROJECT, DESCRIPTION_JIRA_PROJECT);

        final ConfigField issueType = TextInputConfigField.createRequired(JiraDescriptor.KEY_ISSUE_TYPE, LABEL_ISSUE_TYPE, DESCRIPTION_ISSUE_TYPE).addDefaultValue(DEFAULT_ISSUE_TYPE);
        final ConfigField resolveWorkflow = TextInputConfigField.create(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, LABEL_RESOLVE_WORKFLOW_TRANSITION, DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION);
        final ConfigField openWorkflow = TextInputConfigField.create(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, LABEL_OPEN_WORKFLOW_TRANSITION, DESCRIPTION_OPEN_WORKFLOW_TRANSITION);

        final ConfigField resolveStatus = TextInputConfigField.create(JiraDescriptor.KEY_RESOLVE_WORKFLOW_STATUS, LABEL_RESOLVE_WORKFLOW_STATUS, DESCRIPTION_RESOLVE_WORKFLOW_STATUS);
        resolveStatus.setRequiredRelatedFields(Set.of(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION));
        resolveWorkflow.setRequiredRelatedFields(Set.of(JiraDescriptor.KEY_RESOLVE_WORKFLOW_STATUS));

        final ConfigField openStatus = TextInputConfigField.create(JiraDescriptor.KEY_OPEN_WORKFLOW_STATUS, LABEL_OPEN_WORKFLOW_STATUS, DESCRIPTION_OPEN_WORKFLOW_STATUS);
        openStatus.setRequiredRelatedFields(Set.of(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION));
        openWorkflow.setRequiredRelatedFields(Set.of(JiraDescriptor.KEY_OPEN_WORKFLOW_STATUS));

        return List.of(addComments, issueCreator, jiraProjectName, issueType, resolveWorkflow, openWorkflow, resolveStatus, openStatus);
    }
}
