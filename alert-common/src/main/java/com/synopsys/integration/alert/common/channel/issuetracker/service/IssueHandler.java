/**
 * alert-common
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
package com.synopsys.integration.alert.common.channel.issuetracker.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.datastructure.SetMap;
import com.synopsys.integration.exception.IntegrationException;

// TODO consider an additional generic for id type
public abstract class IssueHandler<R> {
    public static final String DESCRIPTION_CONTINUED_TEXT = "(description continued...)";
    public static final String DESCRIPTION_TRUNCATED_TEXT = "... (Comments are disabled.  Description data will be lost. See project information for more data.)";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueContentLengthValidator contentLengthValidator;

    public IssueHandler(IssueContentLengthValidator contentLengthValidator) {
        this.contentLengthValidator = contentLengthValidator;
    }

    public final IssueTrackerResponse createOrUpdateIssues(IssueConfig issueConfig, Collection<IssueTrackerRequest> requests) throws IntegrationException {
        Set<IssueTrackerIssueResponseModel> issueResponseModels = new HashSet<>();
        for (IssueTrackerRequest request : requests) {
            Set<IssueTrackerIssueResponseModel> componentIssueResponseModels = createOrUpdateIssuesPerComponent(issueConfig, request);
            issueResponseModels.addAll(componentIssueResponseModels);
        }

        String statusMessage = createStatusMessage(issueResponseModels);
        return new IssueTrackerResponse(statusMessage, issueResponseModels);
    }

    protected Set<IssueTrackerIssueResponseModel> createOrUpdateIssuesPerComponent(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        Set<IssueTrackerIssueResponseModel> issueResponseModels = new HashSet<>();
        String projectName = issueConfig.getProjectName();

        String issueTitle = request.getRequestContent().getTitle();
        IssueOperation issueOperation = request.getOperation();
        AlertIssueOrigin alertIssueOrigin = request.getAlertIssueOrigin();

        SetMap<String, IssueMissingTransitionException> missingTransitionToExceptions = SetMap.createDefault();
        try {
            if (contentLengthValidator.validateContentLength(request.getRequestContent())) {
                List<R> existingIssues = retrieveExistingIssues(issueConfig.getProjectKey(), request);
                logIssueAction(projectName, request);
                if (!existingIssues.isEmpty()) {
                    Set<R> updatedIssues = updateExistingIssues(existingIssues, issueConfig, request);
                    updatedIssues
                        .stream()
                        .map((R issueResponse) -> createResponseModel(alertIssueOrigin, issueTitle, issueOperation, issueResponse))
                        .forEach(issueResponseModels::add);
                } else if (IssueOperation.OPEN == issueOperation || IssueOperation.UPDATE == issueOperation) {
                    Optional<R> issueModel = createIssue(issueConfig, request);
                    issueModel
                        .map((R issueResponse) -> createResponseModel(alertIssueOrigin, issueTitle, issueOperation, issueResponse))
                        .ifPresent(issueResponseModels::add);
                } else {
                    logger.warn("Expected to find an existing issue, but none existed.");
                }
            }
        } catch (IssueMissingTransitionException e) {
            missingTransitionToExceptions.add(e.getMissingTransition(), e);
        }

        if (!missingTransitionToExceptions.isEmpty()) {
            StringBuilder missingTransitions = new StringBuilder();
            for (Map.Entry<String, Set<IssueMissingTransitionException>> entry : missingTransitionToExceptions.entrySet()) {
                String issueTransitions = entry.getValue()
                                              .stream()
                                              .map(exception -> String.format("%s (Valid Transitions: %s)", exception.getIssueKey(), exception.getValidTransitions()))
                                              .collect(Collectors.joining(" | "));
                missingTransitions.append(String.format("Unable to find the transition: %s, for the issue(s): %s", entry.getKey(), issueTransitions));
            }

            String errorMessage = String.format("For Project: %s. %s.", projectName, missingTransitions.toString());
            throw new IssueTrackerException(errorMessage);
        }
        return issueResponseModels;
    }

    protected abstract Optional<R> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException;

    protected abstract List<R> retrieveExistingIssues(String projectSearchIdentifier, IssueTrackerRequest request) throws IntegrationException;

    protected abstract boolean transitionIssue(R issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException;

    protected abstract void addComment(IssueConfig issueConfig, String issueKey, String comment) throws IntegrationException;

    protected abstract String getIssueKey(R issueModel);

    protected abstract IssueTrackerIssueResponseModel createResponseModel(AlertIssueOrigin alertIssueOrigin, String issueTitle, IssueOperation issueOperation, R issueResponse);

    protected abstract String getIssueTrackerUrl();

    protected abstract void logIssueAction(String issueTrackerProjectName, IssueTrackerRequest request);

    protected final String truncateDescription(String description) {
        String truncatedDescription = StringUtils.substring(description, 0, description.length() - DESCRIPTION_TRUNCATED_TEXT.length());
        return StringUtils.join(truncatedDescription, DESCRIPTION_TRUNCATED_TEXT);
    }

    protected Set<R> updateExistingIssues(List<R> issuesToUpdate, IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        Set<R> updatedIssues = new HashSet<>();
        for (R issue : issuesToUpdate) {
            String issueKey = getIssueKey(issue);
            if (issueConfig.getCommentOnIssues()) {
                IssueContentModel contentModel = request.getRequestContent();
                Collection<String> operationComments = contentModel.getAdditionalComments();
                for (String operationComment : operationComments) {
                    addComment(issueConfig, issueKey, operationComment);
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

    private String createStatusMessage(Collection<IssueTrackerIssueResponseModel> issueResponseModels) {
        if (issueResponseModels.isEmpty()) {
            return "Did not create any issues.";
        }

        String concatenatedKeys = issueResponseModels
                                      .stream()
                                      .map(IssueTrackerIssueResponseModel::getIssueKey)
                                      .collect(Collectors.joining(", "));
        logger.debug("Issues updated: {}", concatenatedKeys);
        return String.format("Successfully created issue at %s. Issue Keys: (%s)", getIssueTrackerUrl(), concatenatedKeys);
    }

}
