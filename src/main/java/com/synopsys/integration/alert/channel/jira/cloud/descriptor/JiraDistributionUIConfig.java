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
package com.synopsys.integration.alert.channel.jira.cloud.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.JiraChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.issuetracker.jira.common.JiraConstants;

@Component
public class JiraDistributionUIConfig extends ChannelDistributionUIConfig {
    public static final String LABEL_ADD_COMMENTS = "Add Comments";
    public static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    public static final String LABEL_JIRA_PROJECT = "Jira Project";
    public static final String LABEL_ISSUE_TYPE = "Issue Type";
    public static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    public static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";

    public static final String DESCRIPTION_ADD_COMMENTS = "If true, this will add comments to the Jira ticket with data describing the latest change.";
    public static final String DESCRIPTION_ISSUE_CREATOR = "The email of the Jira Cloud user to assign as the issue creator field of the Jira issue.";
    public static final String DESCRIPTION_JIRA_PROJECT = "The name or key of the Jira Project for which this job creates and/or updates Jira tickets.";
    public static final String DESCRIPTION_ISSUE_TYPE = "The issue type to open when creating an issue in Jira Cloud.";
    public static final String DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when resolving an issue. This will happen when Alert receives a DELETE operation from a provider. "
                                                                             + "Note: This must be in the 'Done' status category.";
    public static final String DESCRIPTION_OPEN_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when re-opening an issue. This will happen when Alert receives an ADD/UPDATE operation from a provider. "
                                                                          + "Note: This must be in the 'To Do' status category.";

    @Autowired
    public JiraDistributionUIConfig(JiraChannelKey jiraChannelKey, GlobalConfigExistsValidator globalConfigExistsValidator) {
        super(jiraChannelKey, JiraDescriptor.JIRA_LABEL, JiraDescriptor.JIRA_URL, globalConfigExistsValidator, globalConfigExistsValidator);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField addComments = new CheckboxConfigField(JiraDescriptor.KEY_ADD_COMMENTS, LABEL_ADD_COMMENTS, DESCRIPTION_ADD_COMMENTS);
        ConfigField issueCreator = new TextInputConfigField(JiraDescriptor.KEY_ISSUE_CREATOR, LABEL_ISSUE_CREATOR, DESCRIPTION_ISSUE_CREATOR);
        ConfigField jiraProjectName = new TextInputConfigField(JiraDescriptor.KEY_JIRA_PROJECT_NAME, LABEL_JIRA_PROJECT, DESCRIPTION_JIRA_PROJECT).applyRequired(true);

        ConfigField issueType = new TextInputConfigField(JiraDescriptor.KEY_ISSUE_TYPE, LABEL_ISSUE_TYPE, DESCRIPTION_ISSUE_TYPE)
                                    .applyRequired(true)
                                    .applyDefaultValue(JiraConstants.DEFAULT_ISSUE_TYPE);
        ConfigField resolveWorkflow = new TextInputConfigField(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, LABEL_RESOLVE_WORKFLOW_TRANSITION, DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION);
        ConfigField openWorkflow = new TextInputConfigField(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, LABEL_OPEN_WORKFLOW_TRANSITION, DESCRIPTION_OPEN_WORKFLOW_TRANSITION)
                                       .applyRequiredRelatedField(resolveWorkflow.getKey());

        return List.of(addComments, issueCreator, jiraProjectName, issueType, resolveWorkflow, openWorkflow);
    }

}
