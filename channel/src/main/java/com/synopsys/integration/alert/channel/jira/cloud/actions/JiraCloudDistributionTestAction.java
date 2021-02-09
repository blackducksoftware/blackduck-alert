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
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannel;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudContextBuilder;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.common.JiraTestIssueRequestCreator;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraCloudDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final JiraMessageParser jiraMessageParser;
    private final JiraCloudContextBuilder jiraCloudContextBuilder;

    @Autowired
    public JiraCloudDistributionTestAction(JiraCloudChannel jiraChannel, Gson gson, JiraMessageParser jiraMessageParser, JiraCloudContextBuilder jiraCloudContextBuilder) {
        super(jiraChannel);
        this.gson = gson;
        this.jiraMessageParser = jiraMessageParser;
        this.jiraCloudContextBuilder = jiraCloudContextBuilder;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        IssueTrackerContext context = jiraCloudContextBuilder.build(registeredFieldValues);
        JiraTestIssueRequestCreator issueCreator = new JiraTestIssueRequestCreator(registeredFieldValues, jiraMessageParser);
        JiraCloudCreateIssueTestAction testAction = new JiraCloudCreateIssueTestAction((JiraCloudChannel) getDistributionChannel(), gson, issueCreator);
        return testAction.testConfig(context);
    }

}
