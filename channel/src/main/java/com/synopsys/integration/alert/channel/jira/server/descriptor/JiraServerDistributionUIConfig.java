/**
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
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;

@Component
public class JiraServerDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_ADD_COMMENTS = "Add comments";
    private static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    private static final String LABEL_JIRA_PROJECT = "Jira Project";
    private static final String LABEL_ISSUE_TYPE = "Issue Type";
    private static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    private static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";
    private static final String DESCRIPTION_ADD_COMMENTS = "If true, this will add comments to the Jira ticket with data describing the latest change.";
    private static final String DESCRIPTION_ISSUE_CREATOR = "The username of the Jira Server user to assign as the issue creator field of the Jira issue.";
    private static final String DESCRIPTION_JIRA_PROJECT = "The name or key of the Jira Project for which this job creates and/or updates Jira tickets.";
    private static final String DESCRIPTION_ISSUE_TYPE = "The issue type to open when creating an issue in Jira Server.";
    private static final String DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when resolving an issue. This will happen when Alert receives a DELETE operation from a provider. "
                                                                              + "Note: This must be in the 'Done' status category.";
    private static final String DESCRIPTION_OPEN_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when re-opening an issue. This will happen when Alert receives an ADD/UPDATE operation from a provider. "
                                                                           + "Note: This must be in the 'To Do' status category.";

    @Autowired
    public JiraServerDistributionUIConfig(JiraServerChannelKey channelKey) {
        super(channelKey, JiraServerDescriptor.JIRA_LABEL, JiraServerDescriptor.JIRA_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField addComments = new CheckboxConfigField(JiraServerDescriptor.KEY_ADD_COMMENTS, LABEL_ADD_COMMENTS, DESCRIPTION_ADD_COMMENTS);
        ConfigField issueCreator = new TextInputConfigField(JiraServerDescriptor.KEY_ISSUE_CREATOR, LABEL_ISSUE_CREATOR, DESCRIPTION_ISSUE_CREATOR);
        ConfigField jiraProjectName = new TextInputConfigField(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, LABEL_JIRA_PROJECT, DESCRIPTION_JIRA_PROJECT).applyRequired(true);

        ConfigField issueType = new TextInputConfigField(JiraServerDescriptor.KEY_ISSUE_TYPE, LABEL_ISSUE_TYPE, DESCRIPTION_ISSUE_TYPE).applyRequired(true).applyDefaultValue(JiraConstants.DEFAULT_ISSUE_TYPE);
        ConfigField resolveWorkflow = new TextInputConfigField(JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, LABEL_RESOLVE_WORKFLOW_TRANSITION, DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION);
        ConfigField openWorkflow = new TextInputConfigField(JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, LABEL_OPEN_WORKFLOW_TRANSITION, DESCRIPTION_OPEN_WORKFLOW_TRANSITION)
                                       .applyRequiredRelatedField(resolveWorkflow.getKey());

        return List.of(addComments, issueCreator, jiraProjectName, issueType, resolveWorkflow, openWorkflow);
    }

}
