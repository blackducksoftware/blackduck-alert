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
package com.synopsys.integration.alert.channel.jira2.cloud.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
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

public class JiraCloudIssueTransitioner implements IssueTrackerIssueTransitioner<String> {
    public static final String TODO_STATUS_CATEGORY_KEY = "new";
    public static final String DONE_STATUS_CATEGORY_KEY = "done";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final IssueTrackerIssueResponseCreator<String> issueResponseCreator;
    private final JiraCloudIssueCommenter jiraCloudIssueCommenter;

    public JiraCloudIssueTransitioner(
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService,
        IssueTrackerIssueResponseCreator<String> issueResponseCreator,
        JiraCloudIssueCommenter jiraCloudIssueCommenter
    ) {
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.issueResponseCreator = issueResponseCreator;
        this.jiraCloudIssueCommenter = jiraCloudIssueCommenter;
    }

    @Override
    public Optional<IssueTrackerIssueResponseModel> transitionIssue(IssueTransitionModel<String> issueTransitionModel) throws AlertException {
        IssueOperation issueOperation = issueTransitionModel.getIssueOperation();
        ExistingIssueDetails<String> existingIssueDetails = issueTransitionModel.getExistingIssueDetails();
        String issueKey = existingIssueDetails.getIssueKey();

        Optional<IssueTrackerIssueResponseModel> transitionResponse = Optional.empty();

        Optional<String> optionalTransitionName = retrieveJobTransitionName(issueOperation);
        if (optionalTransitionName.isPresent()) {
            String transitionName = optionalTransitionName.get();

            boolean shouldAttemptTransition = isTransitionRequired(issueKey, issueOperation);
            if (shouldAttemptTransition) {
                findAndPerformTransition(issueKey, transitionName);
                jiraCloudIssueCommenter.addComments(issueKey, issueTransitionModel.getPostTransitionComments());
                IssueTrackerIssueResponseModel transitionResponseModel = issueResponseCreator.createIssueResponse(issueTransitionModel.getSource(), existingIssueDetails, issueOperation);
                transitionResponse = Optional.of(transitionResponseModel);
            } else {
                logger.debug("The issue {} is already in the status category that would result from this transition ({}).", issueKey, transitionName);
            }
        } else {
            logger.debug("No transition name was provided so no '{}' transition will be performed. Issue Key: {}", issueOperation.name(), issueKey);
        }

        jiraCloudIssueCommenter.addComments(issueKey, issueTransitionModel.getPostTransitionComments());
        return transitionResponse;
    }

    private Optional<String> retrieveJobTransitionName(IssueOperation transitionType) {
        String transitionName;
        if (IssueOperation.OPEN.equals(transitionType)) {
            transitionName = distributionDetails.getReopenTransition();
        } else {
            transitionName = distributionDetails.getResolveTransition();
        }
        return Optional.ofNullable(transitionName).filter(StringUtils::isNotBlank);
    }

    private boolean isTransitionRequired(String issueKey, IssueOperation issueTransitionType) throws AlertException {
        StatusCategory issueStatusCategory = retrieveIssueStatusCategory(issueKey);
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

    private void findAndPerformTransition(String issueKey, String transitionName) throws AlertException {
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
