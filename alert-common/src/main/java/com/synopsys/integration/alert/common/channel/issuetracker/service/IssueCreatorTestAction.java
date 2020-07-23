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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueTrackerService issueTrackerService;
    private final TestIssueRequestCreator testIssueRequestCreator;

    public IssueCreatorTestAction(IssueTrackerService issueTrackerService, TestIssueRequestCreator testIssueRequestCreator) {
        this.issueTrackerService = issueTrackerService;
        this.testIssueRequestCreator = testIssueRequestCreator;
    }

    public IssueTrackerResponse testConfig(IssueTrackerContext issueTrackerContext) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        IssueTrackerResponse initialTestResult = createAndSendMessage(issueTrackerContext, IssueOperation.OPEN, messageId);
        String initialIssueKey = initialTestResult.getUpdatedIssues()
                                     .stream()
                                     .findFirst()
                                     .map(IssueTrackerIssueResponseModel::getIssueKey)
                                     .orElseThrow(() -> new IssueTrackerException("Failed to create a new issue"));

        Optional<String> optionalResolveTransitionName = issueTrackerContext.getIssueConfig().getResolveTransition().filter(StringUtils::isNotBlank);
        if (optionalResolveTransitionName.isPresent()) {
            String resolveTransitionName = optionalResolveTransitionName.get();
            return testTransitions(issueTrackerContext, messageId, resolveTransitionName, initialIssueKey);
        }
        return initialTestResult;
    }

    protected abstract String getOpenTransitionFieldKey();

    protected abstract String getResolveTransitionFieldKey();

    protected abstract String getTodoStatusFieldKey();

    protected abstract String getDoneStatusFieldKey();

    protected abstract <T> TransitionHandler<T> createTransitionHandler(IssueTrackerContext issueTrackerContext) throws IntegrationException;

    protected abstract void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey);

    private <T> IssueTrackerResponse testTransitions(IssueTrackerContext issueTrackerContext, String messageId, String resolveTransitionName, String initialIssueKey) throws IntegrationException {
        TransitionHandler<T> transitionHandler = createTransitionHandler(issueTrackerContext);
        String fromStatus = "Initial";
        String toStatus = "Resolve";
        Optional<String> possibleSecondIssueKey = Optional.empty();
        try {
            Map<String, String> transitionErrors = new HashMap<>();
            Optional<String> resolveError = validateTransition(transitionHandler, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
            resolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
            IssueTrackerResponse finalResult = createAndSendMessage(issueTrackerContext, IssueOperation.RESOLVE, messageId);

            Optional<String> optionalReopenTransitionName = issueTrackerContext.getIssueConfig().getOpenTransition().filter(StringUtils::isNotBlank);
            if (optionalReopenTransitionName.isPresent()) {
                fromStatus = toStatus;
                toStatus = "Reopen";
                Optional<String> reopenError = validateTransition(transitionHandler, initialIssueKey, optionalReopenTransitionName.get(), getTodoStatusFieldKey());
                reopenError.ifPresent(message -> transitionErrors.put(getOpenTransitionFieldKey(), message));
                IssueTrackerResponse reopenResult = createAndSendMessage(issueTrackerContext, IssueOperation.OPEN, messageId);
                possibleSecondIssueKey = reopenResult.getUpdatedIssues()
                                             .stream()
                                             .findFirst()
                                             .map(IssueTrackerIssueResponseModel::getIssueKey)
                                             .filter(secondIssueKey -> !StringUtils.equals(secondIssueKey, initialIssueKey));

                if (reopenError.isEmpty()) {
                    fromStatus = toStatus;
                    toStatus = "Resolve";
                    Optional<String> reResolveError = validateTransition(transitionHandler, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
                    reResolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
                    finalResult = createAndSendMessage(issueTrackerContext, IssueOperation.RESOLVE, messageId);
                }
            }

            if (transitionErrors.isEmpty()) {
                return finalResult;
            } else {
                throw new AlertFieldException(transitionErrors);
            }
        } catch (AlertFieldException fieldException) {
            safelyCleanUpIssue(issueTrackerContext, initialIssueKey);
            throw fieldException;
        } catch (IssueTrackerException exception) {
            logger.debug(String.format("Error testing %s config", issueTrackerContext.getClass().getSimpleName()), exception);
            String errorMessage = String.format("There were problems transitioning the test issue from the %s status to the %s status: %s", fromStatus, toStatus, exception.getMessage());
            possibleSecondIssueKey.ifPresent(key -> safelyCleanUpIssue(issueTrackerContext, key));
            throw new IssueTrackerException(errorMessage);
        }
    }

    private IssueTrackerResponse createAndSendMessage(IssueTrackerContext issueTrackerContext, IssueOperation operation, String messageId) throws IntegrationException {
        logger.debug("Sending {} test message...", operation.name());
        IssueTrackerRequest request = testIssueRequestCreator.createRequest(operation, messageId);
        List<IssueTrackerRequest> requests = new ArrayList<>(1);
        requests.add(request);
        IssueTrackerResponse messageResult = this.issueTrackerService.sendRequests(issueTrackerContext, requests);
        logger.debug("{} test message sent!", operation.name());
        return messageResult;
    }

    private <T> Optional<String> validateTransition(TransitionHandler<T> transitionHandler, String issueKey, String transitionName, String statusCategoryKey) throws IntegrationException {
        List<T> transitions = transitionHandler.retrieveIssueTransitions(issueKey);
        String validTransitions = transitions
                                      .stream()
                                      .map(transitionHandler::extractTransitionName)
                                      .collect(Collectors.joining(", "));
        Optional<T> optionalTransition = transitions
                                             .stream()
                                             .filter(transition -> transitionHandler.extractTransitionName(transition).equals(transitionName))
                                             .findFirst();
        if (optionalTransition.isPresent()) {
            boolean isValidTransition = transitionHandler.doesTransitionToExpectedStatusCategory(optionalTransition.get(), statusCategoryKey);
            if (!isValidTransition) {
                return Optional.of(String.format("The provided transition would not result in an allowed status category. Available transitions: %s", validTransitions));
            }
        } else {
            return Optional.of(String.format("The provided transition is not possible from the issue state that it would transition from. Available transitions: %s", validTransitions));
        }
        return Optional.empty();
    }

}
