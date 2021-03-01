/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.util;

import java.util.Collections;
import java.util.List;

import com.synopsys.integration.alert.channel.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerTransitionHandler extends JiraTransitionHandler {
    private final IssueService issueService;

    public JiraServerTransitionHandler(IssueService issueService) {
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
