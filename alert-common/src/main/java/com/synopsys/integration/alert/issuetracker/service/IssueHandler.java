/**
 * issuetracker-common
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
package com.synopsys.integration.alert.issuetracker.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.datastructure.SetMap;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueHandler<R> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueContentLengthValidator contentLengthValidator;

    public IssueHandler(IssueContentLengthValidator contentLengthValidator) {
        this.contentLengthValidator = contentLengthValidator;
    }

    public final IssueTrackerResponse createOrUpdateIssues(IssueConfig issueConfig, Collection<IssueTrackerRequest> requests) throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        for (IssueTrackerRequest request : requests) {
            Set<String> issueKeysForMessage = createOrUpdateIssuesPerComponent(issueConfig, request);
            issueKeys.addAll(issueKeysForMessage);
        }

        String statusMessage = createStatusMessage(issueKeys);
        return new IssueTrackerResponse(statusMessage, issueKeys);
    }

    protected Set<String> createOrUpdateIssuesPerComponent(IssueConfig issueConfig, IssueTrackerRequest request)
        throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        String projectName = issueConfig.getProjectName();

        SetMap<String, String> missingTransitionToIssues = SetMap.createDefault();
        try {
            if (contentLengthValidator.validateContentLength(request.getRequestContent())) {
                IssueOperation operation = request.getOperation();

                List<R> existingIssues = retrieveExistingIssues(issueConfig.getProjectKey(), request);
                logIssueAction(projectName, request);
                if (!existingIssues.isEmpty()) {
                    Set<R> updatedIssues = updateExistingIssues(existingIssues, issueConfig, request);
                    updatedIssues
                        .stream()
                        .map(this::getIssueKey)
                        .forEach(issueKeys::add);
                } else if (IssueOperation.OPEN == operation || IssueOperation.UPDATE == operation) {
                    Optional<R> issueModel = createIssue(issueConfig, request);
                    issueModel
                        .map(this::getIssueKey)
                        .ifPresent(issueKeys::add);
                } else {
                    logger.warn("Expected to find an existing issue, but none existed.");
                }
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

            String errorMessage = String.format("For Project: %s. %s.", projectName, missingTransitions.toString());
            throw new IssueTrackerException(errorMessage);
        }
        return issueKeys;
    }

    protected abstract Optional<R> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException;

    protected abstract List<R> retrieveExistingIssues(String projectSearchIdentifier, IssueTrackerRequest request) throws IntegrationException;

    protected abstract boolean transitionIssue(R issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException;

    protected abstract void addComment(String issueKey, String comment) throws IntegrationException;

    protected abstract String getIssueKey(R issueModel);

    protected abstract String getIssueTrackerUrl();

    protected abstract void logIssueAction(String issueTrackerProjectName, IssueTrackerRequest request);

    protected Set<R> updateExistingIssues(List<R> issuesToUpdate, IssueConfig issueConfig, IssueTrackerRequest request)
        throws IntegrationException {
        Set<R> updatedIssues = new HashSet<>();
        for (R issue : issuesToUpdate) {
            String issueKey = getIssueKey(issue);
            if (issueConfig.getCommentOnIssues()) {
                IssueContentModel contentModel = request.getRequestContent();
                Collection<String> operationComments = contentModel.getAdditionalComments();
                for (String operationComment : operationComments) {
                    addComment(issueKey, operationComment);
                }

                updatedIssues.add(issue);
            }

            boolean didUpdateIssue = transitionIssue(issue, issueConfig, request.getOperation());
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

}
