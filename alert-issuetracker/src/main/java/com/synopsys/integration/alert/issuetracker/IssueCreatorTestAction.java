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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerFieldException;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IssueTrackerService issueTrackerService;
    private IssueCreator issueCreator;

    public IssueCreatorTestAction(IssueTrackerService issueTrackerService, IssueCreator issueCreator) {
        this.issueTrackerService = issueTrackerService;
        this.issueCreator = issueCreator;
    }

    public IssueTrackerResponse testConfig(IssueTrackerContext issueTrackerContext) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        IssueTrackerResponse initialTestResult = createAndSendMessage(issueTrackerContext, OperationType.CREATE, messageId);
        String initialIssueKey = initialTestResult.getUpdatedIssueKeys()
                                     .stream()
                                     .findFirst()
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

    protected abstract <T> TransitionValidator<T> createTransitionValidator(IssueTrackerContext issueTrackerContext) throws IntegrationException;

    protected abstract void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey);

    private <T> IssueTrackerResponse testTransitions(IssueTrackerContext issueTrackerContext, String messageId, String resolveTransitionName, String initialIssueKey) throws IntegrationException {
        TransitionValidator<T> transitionValidator = createTransitionValidator(issueTrackerContext);
        String fromStatus = "Initial";
        String toStatus = "Resolve";
        Optional<String> possibleSecondIssueKey = Optional.empty();
        try {
            Map<String, String> transitionErrors = new HashMap<>();
            Optional<String> resolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
            resolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
            IssueTrackerResponse finalResult = createAndSendMessage(issueTrackerContext, OperationType.RESOLVE, messageId);

            Optional<String> optionalReopenTransitionName = issueTrackerContext.getIssueConfig().getOpenTransition().filter(StringUtils::isNotBlank);
            if (optionalReopenTransitionName.isPresent()) {
                fromStatus = toStatus;
                toStatus = "Reopen";
                Optional<String> reopenError = validateTransition(transitionValidator, initialIssueKey, optionalReopenTransitionName.get(), getTodoStatusFieldKey());
                reopenError.ifPresent(message -> transitionErrors.put(getOpenTransitionFieldKey(), message));
                IssueTrackerResponse reopenResult = createAndSendMessage(issueTrackerContext, OperationType.CREATE, messageId);
                possibleSecondIssueKey = reopenResult.getUpdatedIssueKeys()
                                             .stream()
                                             .findFirst()
                                             .filter(secondIssueKey -> !StringUtils.equals(secondIssueKey, initialIssueKey));

                if (reopenError.isEmpty()) {
                    fromStatus = toStatus;
                    toStatus = "Resolve";
                    Optional<String> reResolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
                    reResolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
                    finalResult = createAndSendMessage(issueTrackerContext, OperationType.RESOLVE, messageId);
                }
            }

            if (transitionErrors.isEmpty()) {
                return finalResult;
            } else {
                throw new IssueTrackerFieldException(transitionErrors);
            }
        } catch (IssueTrackerFieldException fieldException) {
            safelyCleanUpIssue(issueTrackerContext, initialIssueKey);
            throw fieldException;
        } catch (IssueTrackerException alertException) {
            logger.debug(String.format("Error testing %s config", issueTrackerContext.getClass().getSimpleName()), alertException);
            String errorMessage = String.format("There were problems transitioning the test issue from the %s status to the %s status: %s", fromStatus, toStatus, alertException.getMessage());
            possibleSecondIssueKey.ifPresent(key -> safelyCleanUpIssue(issueTrackerContext, key));
            throw new IssueTrackerException(errorMessage);
        }
    }

    private IssueTrackerRequest createChannelTestRequest(String messageId) {
        return issueCreator.createRequest(messageId);
    }

    private IssueTrackerResponse createAndSendMessage(IssueTrackerContext issueTrackerContext, OperationType operation, String messageId) throws IntegrationException {
        logger.debug("Sending {} test message...", operation.name());
        IssueTrackerRequest request = createChannelTestRequest(messageId);
        IssueTrackerResponse messageResult = this.issueTrackerService.sendMessage(issueTrackerContext, List.of(request));
        logger.debug("{} test message sent!", operation.name());
        return messageResult;
    }

    private <T> Optional<String> validateTransition(TransitionValidator<T> transitionValidator, String issueKey, String transitionName, String statusCategoryKey) throws IntegrationException {
        Optional<T> transitionComponent = transitionValidator.retrieveIssueTransition(issueKey, transitionName);
        if (transitionComponent.isPresent()) {
            boolean isValidTransition = transitionValidator.doesTransitionToExpectedStatusCategory(transitionComponent.get(), statusCategoryKey);
            if (!isValidTransition) {
                return Optional.of("The provided transition would not result in an allowed status category.");
            }
        } else {
            return Optional.of("The provided transition is not possible from the issue state that it would transition from.");
        }
        return Optional.empty();
    }

}
