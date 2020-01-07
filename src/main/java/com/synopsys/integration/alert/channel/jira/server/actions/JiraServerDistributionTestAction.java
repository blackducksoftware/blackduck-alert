/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.alert.channel.jira.server.JiraServerChannel;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.TransitionValidator;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;

@Component
public class JiraServerDistributionTestAction extends IssueTrackerDistributionTestAction {
    private final Logger logger = LoggerFactory.getLogger(JiraServerDistributionTestAction.class);
    private Gson gson;

    @Autowired
    public JiraServerDistributionTestAction(JiraServerChannel distributionChannel, Gson gson) {
        super(distributionChannel);
        this.gson = gson;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getTodoStatusFieldKey() {
        return JiraTransitionHandler.TODO_STATUS_CATEGORY_KEY;
    }

    @Override
    protected String getDoneStatusFieldKey() {
        return JiraTransitionHandler.DONE_STATUS_CATEGORY_KEY;
    }

    @Override
    protected TransitionValidator<TransitionComponent> createTransitionValidator(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraServerProperties jiraProperties = new JiraServerProperties(fieldAccessor);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        IssueService issueService = jiraServerServiceFactory.createIssueService();
        return new JiraServerTransitionHandler(issueService);
    }

    @Override
    protected void safelyCleanUpIssue(FieldAccessor fieldAccessor, String issueKey) {
        try {
            JiraServerProperties jiraProperties = new JiraServerProperties(fieldAccessor);
            JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            IssueService issueService = jiraServerServiceFactory.createIssueService();
            issueService.deleteIssue(issueKey);
        } catch (IntegrationException e) {
            logger.warn("There was a problem trying to delete a the Jira Server distribution test issue, {}: {}", issueKey, e);
        }
    }
}
