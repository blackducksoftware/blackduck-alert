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
package com.synopsys.integration.alert.common.channel.issuetracker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class IssueTrackerDistributionTestAction extends ChannelDistributionTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public IssueTrackerDistributionTestAction(IssueTrackerChannel issueTrackerDistributionChannel) {
        super(issueTrackerDistributionChannel);
    }

    @Override
    public MessageResult testConfig(String jobId, String destination, FieldAccessor fieldAccessor) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        IssueTrackerMessageResult initialTestResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.ADD, messageId);
        String initialIssueKey = initialTestResult.getUpdatedIssueKeys()
                                     .stream()
                                     .findFirst()
                                     .orElseThrow(() -> new AlertException("Failed to create a new issue"));

        Optional<String> optionalResolveTransitionName = fieldAccessor.getString(getResolveTransitionFieldKey()).filter(StringUtils::isNotBlank);
        if (optionalResolveTransitionName.isPresent()) {
            String resolveTransitionName = optionalResolveTransitionName.get();
            return testTransitions(jobId, fieldAccessor, messageId, resolveTransitionName, initialIssueKey);
        }
        return initialTestResult;
    }

    @Override
    public IssueTrackerChannel getDistributionChannel() {
        return (IssueTrackerChannel) super.getDistributionChannel();
    }

    protected abstract String getOpenTransitionFieldKey();

    protected abstract String getResolveTransitionFieldKey();

    protected abstract String getTodoStatusFieldKey();

    protected abstract String getDoneStatusFieldKey();

    protected abstract <T> TransitionValidator<T> createTransitionValidator(FieldAccessor fieldAccessor) throws IntegrationException;

    protected abstract void safelyCleanUpIssue(FieldAccessor fieldAccessor, String issueKey);

    private <T> IssueTrackerMessageResult testTransitions(String jobId, FieldAccessor fieldAccessor, String messageId, String resolveTransitionName, String initialIssueKey) throws IntegrationException {
        TransitionValidator<T> transitionValidator = createTransitionValidator(fieldAccessor);
        String fromStatus = "Initial";
        String toStatus = "Resolve";
        Optional<String> possibleSecondIssueKey = Optional.empty();
        try {
            Map<String, String> transitionErrors = new HashMap<>();
            Optional<String> resolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
            resolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
            IssueTrackerMessageResult finalResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.DELETE, messageId);

            Optional<String> optionalReopenTransitionName = fieldAccessor.getString(getOpenTransitionFieldKey()).filter(StringUtils::isNotBlank);
            if (optionalReopenTransitionName.isPresent()) {
                fromStatus = toStatus;
                toStatus = "Reopen";
                Optional<String> reopenError = validateTransition(transitionValidator, initialIssueKey, optionalReopenTransitionName.get(), getTodoStatusFieldKey());
                reopenError.ifPresent(message -> transitionErrors.put(getOpenTransitionFieldKey(), message));
                IssueTrackerMessageResult reopenResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.ADD, messageId);
                possibleSecondIssueKey = reopenResult.getUpdatedIssueKeys()
                                             .stream()
                                             .findFirst()
                                             .filter(secondIssueKey -> !StringUtils.equals(secondIssueKey, initialIssueKey));

                if (reopenError.isEmpty()) {
                    fromStatus = toStatus;
                    toStatus = "Resolve";
                    Optional<String> reResolveError = validateTransition(transitionValidator, initialIssueKey, resolveTransitionName, getDoneStatusFieldKey());
                    reResolveError.ifPresent(message -> transitionErrors.put(getResolveTransitionFieldKey(), message));
                    finalResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.DELETE, messageId);
                }
            }

            if (transitionErrors.isEmpty()) {
                return finalResult;
            } else {
                throw new AlertFieldException(transitionErrors);
            }
        } catch (AlertFieldException fieldException) {
            safelyCleanUpIssue(fieldAccessor, initialIssueKey);
            throw fieldException;
        } catch (AlertException alertException) {
            logger.debug(String.format("Error testing %s config", getDistributionChannel().getDestinationName()), alertException);
            String errorMessage = String.format("There were problems transitioning the test issue from the %s status to the %s status: %s", fromStatus, toStatus, alertException.getMessage());
            possibleSecondIssueKey.ifPresent(key -> safelyCleanUpIssue(fieldAccessor, key));
            throw new AlertException(errorMessage);
        }
    }

    private DistributionEvent createChannelTestEvent(String jobId, FieldAccessor fieldAccessor, ItemOperation operation, String messageId) throws AlertException {
        ProviderMessageContent messageContent = createTestNotificationContent(fieldAccessor, operation, messageId);

        String channelName = fieldAccessor.getStringOrEmpty(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        String providerName = fieldAccessor.getStringOrEmpty(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        String formatType = fieldAccessor.getStringOrEmpty(ProviderDistributionUIConfig.KEY_FORMAT_TYPE);

        return new DistributionEvent(jobId, channelName, RestConstants.formatDate(new Date()), providerName, formatType, MessageContentGroup.singleton(messageContent), fieldAccessor);
    }

    private IssueTrackerMessageResult createAndSendMessage(String jobId, FieldAccessor fieldAccessor, ItemOperation operation, String messageId) throws IntegrationException {
        logger.debug("Sending {} test message...", operation.name());
        DistributionEvent resolveIssueEvent = createChannelTestEvent(jobId, fieldAccessor, operation, messageId);
        IssueTrackerMessageResult messageResult = getDistributionChannel().sendMessage(resolveIssueEvent);
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
