/**
 * issuetracker-jira
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
package com.synopsys.integration.issuetracker.jira.common.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionValidator;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;

public abstract class JiraTransitionHandler implements TransitionValidator<TransitionComponent> {
    public static final String TODO_STATUS_CATEGORY_KEY = "new";
    public static final String DONE_STATUS_CATEGORY_KEY = "done";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract void performTransition(String issueKey, IdComponent transitionId) throws IntegrationException;

    protected abstract StatusDetailsComponent getStatusDetails(String issueKey) throws IntegrationException;

    @Override
    public boolean doesTransitionToExpectedStatusCategory(TransitionComponent transition, String expectedStatusCategoryKey) {
        StatusDetailsComponent statusDetails = transition.getTo();
        StatusCategory statusCategory = statusDetails.getStatusCategory();
        return StringUtils.equals(expectedStatusCategoryKey, statusCategory.getKey());
    }

    public boolean transitionIssueIfNecessary(String issueKey, IssueConfig jiraIssueConfig, IssueOperation operation) throws IntegrationException {
        if (IssueOperation.UPDATE.equals(operation)) {
            logger.debug("No transition required for this issue: {}.", issueKey);
            return false;
        }

        Optional<String> transitionName = determineTransitionName(operation, jiraIssueConfig);
        if (transitionName.isPresent()) {
            boolean shouldAttemptTransition = isTransitionRequired(operation, getStatusDetails(issueKey));
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

    private boolean isTransitionRequired(IssueOperation operation, StatusDetailsComponent statusDetailsComponent) throws IntegrationException {
        StatusCategory statusCategory = statusDetailsComponent.getStatusCategory();
        if (IssueOperation.OPEN.equals(operation)) {
            // Should reopen?
            return DONE_STATUS_CATEGORY_KEY.equals(statusCategory.getKey());
        } else if (IssueOperation.RESOLVE.equals(operation)) {
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
            performTransition(issueKey, new IdComponent(transitionId));
        } else {
            throw new IssueMissingTransitionException(issueKey, transitionName);
        }
    }

    private Optional<String> determineTransitionName(IssueOperation operation, IssueConfig jiraIssueConfig) {
        if (!IssueOperation.UPDATE.equals(operation)) {
            if (IssueOperation.RESOLVE.equals(operation)) {
                return jiraIssueConfig.getResolveTransition();
            } else {
                return jiraIssueConfig.getOpenTransition();
            }
        }
        return Optional.empty();
    }
}
