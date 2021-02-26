/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.cloud.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraCloudIssueTransitioner extends IssueTrackerIssueTransitioner<String> {
    public static final String TODO_STATUS_CATEGORY_KEY = "new";
    public static final String DONE_STATUS_CATEGORY_KEY = "done";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;

    public JiraCloudIssueTransitioner(
        JiraCloudIssueCommenter jiraCloudIssueCommenter,
        IssueTrackerIssueResponseCreator<String> issueResponseCreator,
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService
    ) {
        super(jiraCloudIssueCommenter, issueResponseCreator);
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
    }

    @Override
    protected Optional<String> retrieveJobTransitionName(IssueOperation transitionType) {
        String transitionName = null;
        if (IssueOperation.OPEN.equals(transitionType)) {
            transitionName = distributionDetails.getReopenTransition();
        } else if (IssueOperation.RESOLVE.equals(transitionType)) {
            transitionName = distributionDetails.getResolveTransition();
        }
        return Optional.ofNullable(transitionName).filter(StringUtils::isNotBlank);
    }

    @Override
    protected boolean isTransitionRequired(ExistingIssueDetails<String> existingIssueDetails, IssueOperation issueTransitionType) throws AlertException {
        StatusCategory issueStatusCategory = retrieveIssueStatusCategory(existingIssueDetails.getIssueKey());
        String statusCategoryKey = issueStatusCategory.getKey();
        if (IssueOperation.OPEN.equals(issueTransitionType)) {
            // Should reopen?
            return DONE_STATUS_CATEGORY_KEY.equals(statusCategoryKey);
        } else if (IssueOperation.RESOLVE.equals(issueTransitionType)) {
            // Should resolve?
            return TODO_STATUS_CATEGORY_KEY.equals(statusCategoryKey);
        }
        return false;
    }

    private StatusCategory retrieveIssueStatusCategory(String issueKey) throws AlertException {
        try {
            StatusDetailsComponent issueStatus = issueService.getStatus(issueKey);
            return issueStatus.getStatusCategory();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve issue status from Jira. Issue Key: %s", issueKey), e);
        }
    }

    @Override
    protected void findAndPerformTransition(ExistingIssueDetails<String> existingIssueDetails, String transitionName) throws AlertException {
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

    private List<TransitionComponent> retrieveTransitions(String issueKey) throws AlertException {
        try {
            TransitionsResponseModel transitionsResponse = issueService.getTransitions(issueKey);
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
            issueService.transitionIssue(issueRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to transition issue in Jira. Issue Key: %s", issueKey), e);
        }
    }

}
