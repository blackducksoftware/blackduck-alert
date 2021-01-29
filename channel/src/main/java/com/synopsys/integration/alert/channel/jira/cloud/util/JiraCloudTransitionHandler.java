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
package com.synopsys.integration.alert.channel.jira.cloud.util;

import java.util.Collections;
import java.util.List;

import com.synopsys.integration.alert.channel.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;

public class JiraCloudTransitionHandler extends JiraTransitionHandler {
    private final IssueService issueService;

    public JiraCloudTransitionHandler(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public List<TransitionComponent> retrieveIssueTransitions(String issueKey) throws IntegrationException {
        return issueService.getTransitions(issueKey).getTransitions();
    }

    @Override
    public String extractTransitionName(TransitionComponent transition) {
        return transition.getName();
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
