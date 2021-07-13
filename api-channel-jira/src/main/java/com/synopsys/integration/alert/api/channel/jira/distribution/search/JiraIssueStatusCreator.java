/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraIssueStatusCreator {
    private final String resolveTransition;
    private final String reopenTransition;

    public JiraIssueStatusCreator(String resolveTransition, String reopenTransition) {
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
    }

    public IssueStatus createIssueStatus(JiraSearcherResponseModel issue, ThrowingFunction<String, StatusDetailsComponent, IntegrationException> statusCategoryRetriever,
        ThrowingFunction<String, TransitionsResponseModel, IntegrationException> transitionsRetriever) {
        try {
            //Get the status category from the issue we are extracting
            String issueKey = issue.getIssueKey();
            StatusCategory issueStatusCategory = retrieveIssueStatusCategory(issueKey, statusCategoryRetriever);

            //get the available transitions and compare to see which is currently being used by the status category above.
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

    //TODO: The method above does NOT use the StatusCategory. If there is no reason for it going forward, it  should be removed with this method
    //  In addition, it should be removed from JiraSearcher and is children
    private StatusCategory retrieveIssueStatusCategory(String issueKey, ThrowingFunction<String, StatusDetailsComponent, IntegrationException> statusCategoryRetriever) throws AlertException {
        try {
            StatusDetailsComponent issueStatus = statusCategoryRetriever.apply(issueKey);
            return issueStatus.getStatusCategory();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve issue status from Jira. Issue Key: %s", issueKey), e);
        }
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
