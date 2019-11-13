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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;

//TODO rename to createIssueTestAction
public abstract class IssueTrackerDistributionTestAction {
    public static final String KEY_CUSTOM_TOPIC = "channel.common.custom.message.topic";
    public static final String KEY_CUSTOM_MESSAGE = "channel.common.custom.message.content";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IssueTrackerService issueTrackerService;

    public IssueTrackerDistributionTestAction(IssueTrackerService issueTrackerService) {
        this.issueTrackerService = issueTrackerService;
    }

    public IssueTrackerMessageResult testConfig(IssueTrackerContext issueTrackerContext, String topic, String messageContent) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        IssueTrackerMessageResult initialTestResult = createAndSendMessage(issueTrackerContext, ItemOperation.ADD, messageId, topic, messageContent);
        String initialIssueKey = initialTestResult.getUpdatedIssueKeys()
                                     .stream()
                                     .findFirst()
                                     .orElseThrow(() -> new AlertException("Failed to create a new issue"));

        Optional<String> optionalResolveTransitionName = issueTrackerContext.getIssueConfig().getResolveTransition().filter(StringUtils::isNotBlank);
        if (optionalResolveTransitionName.isPresent()) {
            String resolveTransitionName = optionalResolveTransitionName.get();
            return testTransitions(issueTrackerContext, messageId, resolveTransitionName, initialIssueKey, topic, messageContent);
        }
        return initialTestResult;
    }

    protected abstract String getOpenTransitionFieldKey();

    protected abstract String getResolveTransitionFieldKey();

    protected abstract String getTodoStatusFieldKey();

    protected abstract String getDoneStatusFieldKey();

    protected abstract <T> TransitionValidator<T> createTransitionValidator(IssueTrackerContext issueTrackerContext) throws IntegrationException;

    protected abstract void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey);

    private <T> IssueTrackerMessageResult testTransitions(IssueTrackerContext issueTrackerContext, String messageId, String resolveTransitionName, String initialIssueKey, String topic, String messageContent) throws IntegrationException {
        TransitionValidator<T> transitionValidator = createTransitionValidator(issueTrackerContext);
        String fromStatus = "Initial";
        String toStatus = "Resolve";
        Optional<String> possibleSecondIssueKey = Optional.empty();
        try {
            Map<String, String> transitionErrors = new HashMap<>();
            Optional<String> resolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
            resolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
            IssueTrackerMessageResult finalResult = createAndSendMessage(issueTrackerContext, ItemOperation.DELETE, messageId, topic, messageContent);

            Optional<String> optionalReopenTransitionName = issueTrackerContext.getIssueConfig().getOpenTransition().filter(StringUtils::isNotBlank);
            if (optionalReopenTransitionName.isPresent()) {
                fromStatus = toStatus;
                toStatus = "Reopen";
                Optional<String> reopenError = validateTransition(transitionValidator, initialIssueKey, optionalReopenTransitionName.get(), getTodoStatusFieldKey());
                reopenError.ifPresent(message -> transitionErrors.put(getOpenTransitionFieldKey(), message));
                IssueTrackerMessageResult reopenResult = createAndSendMessage(issueTrackerContext, ItemOperation.ADD, messageId, topic, messageContent);
                possibleSecondIssueKey = reopenResult.getUpdatedIssueKeys()
                                             .stream()
                                             .findFirst()
                                             .filter(secondIssueKey -> !StringUtils.equals(secondIssueKey, initialIssueKey));

                if (reopenError.isEmpty()) {
                    fromStatus = toStatus;
                    toStatus = "Resolve";
                    Optional<String> reResolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
                    reResolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
                    finalResult = createAndSendMessage(issueTrackerContext, ItemOperation.DELETE, messageId, topic, messageContent);
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
        } catch (AlertException alertException) {
            logger.debug(String.format("Error testing %s config", issueTrackerContext.getClass().getSimpleName()), alertException);
            String errorMessage = String.format("There were problems transitioning the test issue from the %s status to the %s status: %s", fromStatus, toStatus, alertException.getMessage());
            possibleSecondIssueKey.ifPresent(key -> safelyCleanUpIssue(issueTrackerContext, key));
            throw new AlertException(errorMessage);
        }
    }

    private IssueTrackerRequest createChannelTestRequest(IssueTrackerContext issueTrackerContext, ItemOperation operation, String messageId, String topic, String customMessage) throws AlertException {
        ProviderMessageContent messageContent = createTestNotificationContent(issueTrackerContext, operation, messageId, topic, customMessage);

        return new IssueTrackerRequest(issueTrackerContext, MessageContentGroup.singleton(messageContent));
    }

    private IssueTrackerMessageResult createAndSendMessage(IssueTrackerContext issueTrackerContext, ItemOperation operation, String messageId, String topic, String messageContent) throws IntegrationException {
        logger.debug("Sending {} test message...", operation.name());
        IssueTrackerRequest request = createChannelTestRequest(issueTrackerContext, operation, messageId, topic, messageContent);
        IssueTrackerMessageResult messageResult = this.issueTrackerService.sendMessage(request);
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

    private ProviderMessageContent createTestNotificationContent(IssueTrackerContext issueTrackerContext, ItemOperation operation, String messageId, String customTopic, String customMessage) throws AlertException {
        return new ProviderMessageContent.Builder()
                   .applyProvider("Alert")
                   .applyTopic("Test Topic", customTopic)
                   .applySubTopic("Test SubTopic", "Test message sent by Alert")
                   .applyComponentItem(createTestComponentItem(operation, messageId, customMessage))
                   .build();
    }

    private ComponentItem createTestComponentItem(ItemOperation operation, String messageId, String customMessage) throws AlertException {
        return new ComponentItem.Builder()
                   .applyOperation(operation)
                   .applyCategory("Test Category")
                   .applyComponentData("Message ID", messageId)
                   .applyCategoryItem("Details", customMessage)
                   .applyNotificationId(1L)
                   .build();
    }

}
