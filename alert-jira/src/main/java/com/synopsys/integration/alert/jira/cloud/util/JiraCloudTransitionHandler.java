/**
 * alert-jira
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
package com.synopsys.integration.alert.jira.cloud.util;

import java.util.Collections;
import java.util.Optional;

import com.synopsys.integration.alert.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraCloudTransitionHandler extends JiraTransitionHandler {
    private IssueService issueService;

    public JiraCloudTransitionHandler(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public Optional<TransitionComponent> retrieveIssueTransition(String issueKey, String transitionName) throws IntegrationException {
        TransitionsResponseModel transitions = issueService.getTransitions(issueKey);
        return transitions.findFirstTransitionByName(transitionName);
    }

    @Override
    protected void performTransition(String issueKey, IdComponent transitionId) throws IntegrationException {
        IssueRequestModel issueRequestModel = new IssueRequestModel(issueKey, transitionId, new IssueRequestModelFieldsBuilder(), Collections.emptyMap(), Collections.emptyList());
        issueService.transitionIssue(issueRequestModel);
    }

    @Override
    protected StatusDetailsComponent getStatusDetails(String issueKey) throws IntegrationException {
        return issueService.getStatus(issueKey);
    }
}
