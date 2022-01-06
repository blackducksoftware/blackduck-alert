/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraIssueStatusCreator {
    private final String resolveTransition;
    private final String reopenTransition;

    public JiraIssueStatusCreator(String resolveTransition, String reopenTransition) {
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
    }

    public IssueStatus createIssueStatus(JiraSearcherResponseModel issue, ThrowingFunction<String, TransitionsResponseModel, IntegrationException> transitionsRetriever) {
        try {
            String issueKey = issue.getIssueKey();
            List<TransitionComponent> issueTransitions = retrieveTransitions(issueKey, transitionsRetriever);
            for (TransitionComponent transition : issueTransitions) {
                String transitionName = transition.getName();
                if (transitionName.equals(resolveTransition)) {
                    return IssueStatus.RESOLVABLE;
                } else if (transitionName.equals(reopenTransition)) {
                    return IssueStatus.REOPENABLE;
                }
            }
        } catch (AlertException e) {
            return IssueStatus.UNKNOWN;
        }
        return IssueStatus.UNKNOWN;
    }

    private List<TransitionComponent> retrieveTransitions(String issueKey, ThrowingFunction<String, TransitionsResponseModel, IntegrationException> transitionsRetriever) throws AlertException {
        try {
            TransitionsResponseModel transitionsResponse = transitionsRetriever.apply(issueKey);
            return transitionsResponse.getTransitions();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve transitions from Jira. Issue Key: %s", issueKey), e);
        }
    }
}
