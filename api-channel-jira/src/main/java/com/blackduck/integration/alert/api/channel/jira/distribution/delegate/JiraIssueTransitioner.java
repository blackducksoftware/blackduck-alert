/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCommenter;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueTransitioner;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.IdComponent;
import com.blackduck.integration.jira.common.model.components.StatusCategory;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;
import com.blackduck.integration.jira.common.model.components.TransitionComponent;
import com.blackduck.integration.jira.common.model.request.IssueRequestModel;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;
import com.blackduck.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;

public abstract class JiraIssueTransitioner extends IssueTrackerIssueTransitioner<String> {
    public static final String TODO_STATUS_CATEGORY_KEY = "new";
    public static final String DONE_STATUS_CATEGORY_KEY = "done";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final @Nullable String resolveTransitionName;
    private final @Nullable String reopenTransitionName;

    protected JiraIssueTransitioner(
        IssueTrackerIssueCommenter<String> commenter,
        IssueTrackerIssueResponseCreator issueResponseCreator,
        @Nullable String resolveTransitionName,
        @Nullable String reopenTransitionName
    ) {
        super(commenter, issueResponseCreator);
        this.resolveTransitionName = resolveTransitionName;
        this.reopenTransitionName = reopenTransitionName;
    }

    protected abstract StatusDetailsComponent fetchIssueStatus(String issueKey) throws IntegrationException;

    protected abstract TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;

    protected abstract void executeTransitionRequest(IssueRequestModel issueRequestModel) throws IntegrationException;

    @Override
    protected final Optional<String> retrieveJobTransitionName(IssueOperation transitionType) {
        String transitionName = null;
        if (IssueOperation.OPEN.equals(transitionType)) {
            transitionName = reopenTransitionName;
        } else if (IssueOperation.RESOLVE.equals(transitionType)) {
            transitionName = resolveTransitionName;
        }
        return Optional.ofNullable(transitionName).filter(StringUtils::isNotBlank);
    }

    @Override
    protected final boolean isTransitionRequired(ExistingIssueDetails<String> existingIssueDetails, IssueOperation transitionType) throws AlertException {
        StatusCategory issueStatusCategory = retrieveIssueStatusCategory(existingIssueDetails.getIssueKey());
        String statusCategoryKey = issueStatusCategory.getKey();
        if (IssueOperation.OPEN.equals(transitionType)) {
            // Should reopen?
            return DONE_STATUS_CATEGORY_KEY.equals(statusCategoryKey);
        } else if (IssueOperation.RESOLVE.equals(transitionType)) {
            // Should resolve?
            return !DONE_STATUS_CATEGORY_KEY.equals(statusCategoryKey);
        }
        return false;
    }

    @Override
    protected final void findAndPerformTransition(ExistingIssueDetails<String> existingIssueDetails, String transitionName) throws AlertException {
        String issueKey = existingIssueDetails.getIssueKey();
        logger.debug("Attempting the transition '{}' on the issue '{}'", transitionName, issueKey);
        List<TransitionComponent> issueTransitions = retrieveTransitions(issueKey);
        Optional<IdComponent> foundTransitionId = findTransitionIdByTransitionName(issueTransitions, transitionName);
        if (foundTransitionId.isPresent()) {
            performTransition(issueKey, foundTransitionId.get());
        } else {
            List<String> validTransitions = gatherValidTransitionNames(issueTransitions);
            throw new IssueMissingTransitionException(issueKey, transitionName, validTransitions);
        }
    }

    private StatusCategory retrieveIssueStatusCategory(String issueKey) throws AlertException {
        try {
            StatusDetailsComponent issueStatus = fetchIssueStatus(issueKey);
            return issueStatus.getStatusCategory();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve issue status from Jira. Issue Key: %s", issueKey), e);
        }
    }

    private List<TransitionComponent> retrieveTransitions(String issueKey) throws AlertException {
        try {
            TransitionsResponseModel transitionsResponse = fetchIssueTransitions(issueKey);
            return transitionsResponse.getTransitions();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve transitions from Jira. Issue Key: %s", issueKey), e);
        }
    }

    private Optional<IdComponent> findTransitionIdByTransitionName(List<TransitionComponent> transitions, String transitionName) {
        return transitions
                   .stream()
                   .filter(transitionComp -> transitionComp.getName().equals(transitionName))
                   .map(TransitionComponent::getId)
                   .map(IdComponent::new)
                   .findFirst();
    }

    private List<String> gatherValidTransitionNames(List<TransitionComponent> transitions) {
        return transitions
                   .stream()
                   .map(TransitionComponent::getName)
                   .collect(Collectors.toList());
    }

    private void performTransition(String issueKey, IdComponent transitionId) throws AlertException {
        IssueRequestModel issueRequestModel = new IssueRequestModel(issueKey, transitionId, new IssueRequestModelFieldsBuilder(), Map.of(), List.of());
        try {
            executeTransitionRequest(issueRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to transition issue in Jira. Issue Key: %s", issueKey), e);
        }
    }

}
