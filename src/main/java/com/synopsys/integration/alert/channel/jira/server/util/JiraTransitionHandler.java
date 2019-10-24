/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.server.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.issuetracker.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.channel.issuetracker.TransitionValidator;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraTransitionHandler implements TransitionValidator<TransitionComponent> {
    public static final String TODO_STATUS_CATEGORY_KEY = "new";
    public static final String DONE_STATUS_CATEGORY_KEY = "done";

    private final Logger logger = LoggerFactory.getLogger(JiraTransitionHandler.class);
    private IssueService issueService;

    public JiraTransitionHandler(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public boolean doesTransitionToExpectedStatusCategory(TransitionComponent transition, String expectedStatusCategoryKey) {
        StatusDetailsComponent statusDetails = transition.getTo();
        StatusCategory statusCategory = statusDetails.getStatusCategory();
        return StringUtils.equals(expectedStatusCategoryKey, statusCategory.getKey());
    }

    @Override
    public Optional<TransitionComponent> retrieveIssueTransition(String issueKey, String transitionName) throws IntegrationException {
        TransitionsResponseModel transitions = issueService.getTransitions(issueKey);
        return transitions.findFirstTransitionByName(transitionName);
    }

    public boolean transitionIssueIfNecessary(String issueKey, IssueConfig jiraIssueConfig, ItemOperation operation) throws IntegrationException {
        if (ItemOperation.UPDATE.equals(operation)) {
            logger.debug("No transition required for this issue: {}.", issueKey);
            return false;
        }

        Optional<String> transitionName = determineTransitionName(operation, jiraIssueConfig);
        if (transitionName.isPresent()) {
            boolean shouldAttemptTransition = isTransitionRequired(issueKey, operation);
            if (shouldAttemptTransition) {
                performTransition(issueKey, transitionName.get());
                return true;
            } else {
                logger.debug("The issue {} is already in the status category that would result from this transition ({}).", issueKey, transitionName);
            }
        } else {
            logger.debug("No transition name was provided so no transition will be performed for this operation: {}.", operation);
        }
        return false;
    }

    private boolean isTransitionRequired(String issueKey, ItemOperation operation) throws IntegrationException {
        StatusDetailsComponent statusDetailsComponent = issueService.getStatus(issueKey);
        StatusCategory statusCategory = statusDetailsComponent.getStatusCategory();
        if (ItemOperation.ADD.equals(operation)) {
            // Should reopen?
            return DONE_STATUS_CATEGORY_KEY.equals(statusCategory.getKey());
        } else if (ItemOperation.DELETE.equals(operation)) {
            // Should resolve?
            return TODO_STATUS_CATEGORY_KEY.equals(statusCategory.getKey());
        }
        return false;
    }

    private void performTransition(String issueKey, String transitionName) throws IntegrationException {
        logger.debug("Attempting the transition '{}' on the issue '{}'", transitionName, issueKey);
        Optional<TransitionComponent> firstTransitionByName = retrieveIssueTransition(issueKey, transitionName);
        if (firstTransitionByName.isPresent()) {
            String transitionId = firstTransitionByName.map(TransitionComponent::getId).get();
            IssueRequestModel issueRequestModel = new IssueRequestModel(issueKey, new IdComponent(transitionId), new IssueRequestModelFieldsBuilder(), Map.of(), List.of());
            issueService.transitionIssue(issueRequestModel);
        } else {
            throw new IssueMissingTransitionException(issueKey, transitionName);
        }
    }

    private Optional<String> determineTransitionName(ItemOperation operation, IssueConfig jiraIssueConfig) {
        if (!ItemOperation.UPDATE.equals(operation)) {
            if (ItemOperation.DELETE.equals(operation)) {
                return jiraIssueConfig.getResolveTransition();
            } else {
                return jiraIssueConfig.getOpenTransition();
            }
        }
        return Optional.empty();
    }

}
