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

    public static final String DESCRIPTION_ADD_COMMENTS = "If true, this will add comments to the Jira ticket with data describing the latest change to the ticket due to the job processing.";
    public static final String DESCRIPTION_ISSUE_CREATOR = "The user name of the Jira Cloud user to assign to the issue creator field of the Jira ticket.";
    public static final String DESCRIPTION_JIRA_PROJECT = "The name of the Jira Project this job creates and/or updates Jira tickets";

    @Autowired
    public JiraDistributionUIConfig(@Lazy final DescriptorMap descriptorMap) {
        super(JiraChannel.COMPONENT_NAME, JiraDescriptor.JIRA_LABEL, JiraDescriptor.JIRA_URL, JiraDescriptor.JIRA_ICON, descriptorMap);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField addComments = CheckboxConfigField.create(JiraDescriptor.KEY_ADD_COMMENTS, LABEL_ADD_COMMENTS, DESCRIPTION_ADD_COMMENTS);
        final ConfigField issueCreator = TextInputConfigField.create(JiraDescriptor.KEY_ISSUE_CREATOR, LABEL_ISSUE_CREATOR, DESCRIPTION_ISSUE_CREATOR);
        final ConfigField jiraProjectName = TextInputConfigField.createRequired(JiraDescriptor.KEY_JIRA_PROJECT_NAME, LABEL_JIRA_PROJECT, DESCRIPTION_JIRA_PROJECT);
        return List.of(addComments, issueCreator, jiraProjectName);
    }
}
