/**
 * alert-issuetracker
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
package com.synopsys.integration.alert.issuetracker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueHandler<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final IssueTrackerResponse createOrUpdateIssues(IssueConfig issueConfig, Collection<IssueContentModel> contentModels) throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        for (IssueContentModel messageContent : contentModels) {
            Set<String> issueKeysForMessage = createOrUpdateIssuesPerComponent(issueConfig, messageContent);
            issueKeys.addAll(issueKeysForMessage);
        }

        String statusMessage = createStatusMessage(issueKeys);
        return new IssueTrackerResponse(statusMessage, issueKeys);
    }

    protected Set<String> createOrUpdateIssuesPerComponent(IssueConfig issueConfig, IssueContentModel contentModel)
        throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        String projectName = issueConfig.getProjectName();
        IssueProperties issueProperties = contentModel.getIssueProperties();

        SetMap<String, String> missingTransitionToIssues = SetMap.createDefault();
        try {
            OperationType operation = contentModel.getOperation();

            List<T> existingIssues = retrieveExistingIssues(issueConfig.getProjectKey(), issueProperties);
            logIssueAction(operation, projectName, issueProperties);
            if (!existingIssues.isEmpty()) {
                Set<T> updatedIssues = updateExistingIssues(existingIssues, issueConfig, contentModel);
                updatedIssues
                    .stream()
                    .map(this::getIssueKey)
                    .forEach(issueKeys::add);
            } else if (OperationType.CREATE == operation || OperationType.UPDATE == operation) {
                T issueModel = createIssue(issueConfig, contentModel);
                String issueKey = getIssueKey(issueModel);
                issueKeys.add(issueKey);
            } else {
                logger.warn("Expected to find an existing issue, but none existed.");
            }
        } catch (IssueMissingTransitionException e) {
            missingTransitionToIssues.add(e.getTransition(), e.getIssueKey());
        }

        if (!missingTransitionToIssues.isEmpty()) {
            StringBuilder missingTransitions = new StringBuilder();
            for (Map.Entry<String, Set<String>> entry : missingTransitionToIssues.entrySet()) {
                String issues = StringUtils.join(entry.getValue(), ", ");
                missingTransitions.append(String.format("Unable to find the transition: %s, for the issue(s): %s", entry.getKey(), issues));
            }

            String errorMessage = String.format("For Provider: %s. Project: %s. %s.", issueProperties.getProvider(), projectName, missingTransitions.toString());
            throw new IssueTrackerException(errorMessage);
        }
        return issueKeys;
    }

    protected abstract T createIssue(IssueConfig issueConfig, IssueContentModel contentModel) throws IntegrationException;

    protected abstract List<T> retrieveExistingIssues(String projectSearchIdentifier, IssueProperties issueProperties) throws IntegrationException;

    protected abstract boolean transitionIssue(T issueModel, IssueConfig issueConfig, OperationType operation) throws IntegrationException;

    protected abstract void addComment(String issueKey, String comment) throws IntegrationException;

    protected abstract String getIssueKey(T issueModel);

    protected abstract String getIssueTrackerUrl();

    protected Set<T> updateExistingIssues(List<T> issuesToUpdate, IssueConfig issueConfig, IssueContentModel issueContentModel)
        throws IntegrationException {
        Set<T> updatedIssues = new HashSet<>();
        for (T issue : issuesToUpdate) {
            String issueKey = getIssueKey(issue);
            if (issueConfig.getCommentOnIssues()) {
                Collection<String> operationComments = issueContentModel.getAdditionalComments();
                for (String operationComment : operationComments) {
                    addComment(issueKey, operationComment);
                }
                updatedIssues.add(issue);
            }

            boolean didUpdateIssue = transitionIssue(issue, issueConfig, issueContentModel.getOperation());
            if (didUpdateIssue) {
                updatedIssues.add(issue);
            }
        }
        return updatedIssues;
    }

    private String createStatusMessage(Collection<String> issueKeys) {
        if (issueKeys.isEmpty()) {
            return "Did not create any issues.";
        }
        String concatenatedKeys = String.join(", ", issueKeys);
        logger.debug("Issues updated: {}", concatenatedKeys);
        return String.format("Successfully created issue at %s. Issue Keys: (%s)", getIssueTrackerUrl(), concatenatedKeys);
    }

    private void logIssueAction(OperationType operation, String issueTrackerProjectName, IssueProperties issueProperties) {
        String issueTrackerProjectVersion = issueProperties.getSubTopicValue() != null ? issueProperties.getSubTopicValue() : "unknown";
        String arbitraryItemSubComponent = issueProperties.getSubComponentValue() != null ? issueProperties.getSubTopicValue() : "unknown";
        logger.debug("Attempting the {} action on the project {}. Provider: {}, Provider Project: {}[{}]. Category: {}, Component: {}, SubComponent: {}.",
            operation.name(), issueTrackerProjectName, issueProperties.getProvider(), issueProperties.getTopicValue(), issueTrackerProjectVersion, issueProperties.getCategory(), issueProperties.getComponentValue(),
            arbitraryItemSubComponent);
    }
}
