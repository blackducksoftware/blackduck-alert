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
package com.synopsys.integration.alert.channel.jira.server.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.common.JiraTestIssueRequestCreator;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannel;
import com.synopsys.integration.alert.channel.jira.server.JiraServerContextBuilder;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraServerDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final JiraMessageParser jiraMessageParser;
    private final JiraServerContextBuilder jiraServerContextBuilder;

    @Autowired
    public JiraServerDistributionTestAction(JiraServerChannel jiraServerChannel, Gson gson, JiraMessageParser jiraMessageParser,
        JiraServerContextBuilder jiraServerContextBuilder) {
        super(jiraServerChannel);
        this.gson = gson;
        this.jiraMessageParser = jiraMessageParser;
        this.jiraServerContextBuilder = jiraServerContextBuilder;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        IssueTrackerContext context = jiraServerContextBuilder.build(registeredFieldValues);
        JiraTestIssueRequestCreator issueCreator = new JiraTestIssueRequestCreator(registeredFieldValues, jiraMessageParser);
        JiraServerCreateIssueTestAction testAction = new JiraServerCreateIssueTestAction((JiraServerChannel) getDistributionChannel(), gson, issueCreator);
        return testAction.testConfig(context);
    }

}
