/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.TransitionComponent;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraIssueStatusCreatorTest {
    private final String resolveTransition = "Done";
    private final String reopenTransition = "Reopen";

    private final JiraIssueStatusCreator jiraIssueStatusCreator = new JiraIssueStatusCreator(resolveTransition, reopenTransition);

    @Test
    public void createIssueStatusResolvableTest() {
        //The transition is used instead of the issueKey which is normally used to get the transition states from Jira
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(null, resolveTransition, null, null);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(jiraSearcherResponseModel, this::fetchIssueTransitions);

        assertEquals(IssueStatus.RESOLVABLE, issueStatus);
    }

    @Test
    public void createIssueStatusReopenableTest() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(null, reopenTransition, null, null);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(jiraSearcherResponseModel, this::fetchIssueTransitions);

        assertEquals(IssueStatus.REOPENABLE, issueStatus);
    }

    @Test
    public void createIssueStatusUnknownTest() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(null, "unknown", null, null);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(jiraSearcherResponseModel, this::fetchIssueTransitions);

        assertEquals(IssueStatus.UNKNOWN, issueStatus);
    }

    @Test
    public void createIssueStatusExceptionTest() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(null, "unknown", null, null);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(jiraSearcherResponseModel, this::fetchIssueTransitionsException);

        assertEquals(IssueStatus.UNKNOWN, issueStatus);
    }

    private TransitionsResponseModel fetchIssueTransitions(String testTransitionName) {
        TransitionComponent transitionComponent = new TransitionComponent(null, testTransitionName, null, null, null, null, null, null);
        TransitionsResponseModel transitionsResponseModel = new TransitionsResponseModel(null, List.of(transitionComponent));
        return transitionsResponseModel;
    }

    private TransitionsResponseModel fetchIssueTransitionsException(String testTransitionName) throws IntegrationException {
        throw new IntegrationException("Exception for test");
    }
}
