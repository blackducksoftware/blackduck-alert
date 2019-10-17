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
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraChannel;
import com.synopsys.integration.alert.channel.jira.cloud.JiraProperties;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraTransitionHandler;
import com.synopsys.integration.alert.common.action.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerMessageResult;
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
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.rest.RestConstants;

@Component
public class JiraDistributionTestAction extends ChannelDistributionTestAction {
    private final Logger logger = LoggerFactory.getLogger(JiraDistributionTestAction.class);
    private Gson gson;

    @Autowired
    public JiraDistributionTestAction(JiraChannel jiraChannel, Gson gson) {
        super(jiraChannel);
        this.gson = gson;
    }

    @Override
    public MessageResult testConfig(String jobId, String destination, FieldAccessor fieldAccessor) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        IssueTrackerMessageResult initialTestResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.ADD, messageId);
        String initialIssueKey = initialTestResult.getUpdatedIssueKeys()
                                     .stream()
                                     .findFirst()
                                     .orElseThrow(() -> new AlertException("Failed to create a new issue"));

        JiraProperties jiraProperties = new JiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        IssueService issueService = jiraCloudServiceFactory.createIssueService();

        Optional<String> optionalResolveTransitionName = fieldAccessor.getString(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION).filter(StringUtils::isNotBlank);
        if (optionalResolveTransitionName.isPresent()) {
            String resolveTransitionName = optionalResolveTransitionName.get();
            return testTransitions(jobId, fieldAccessor, messageId, issueService, resolveTransitionName, initialIssueKey);
        }
        return initialTestResult;
    }

    private IssueTrackerMessageResult testTransitions(String jobId, FieldAccessor fieldAccessor, String messageId, IssueService issueService, String resolveTransitionName, String initialIssueKey) throws IntegrationException {
        JiraTransitionHandler transitionHelper = new JiraTransitionHandler(issueService);
        String fromStatus = "Initial";
        String toStatus = "Resolve";
        Optional<String> possibleSecondIssueKey = Optional.empty();
        try {
            Map<String, String> transitionErrors = new HashMap<>();
            Optional<String> resolveError = validateTransition(transitionHelper, initialIssueKey, resolveTransitionName, JiraTransitionHandler.DONE_STATUS_CATEGORY_KEY);
            resolveError.ifPresent(message -> transitionErrors.put(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, message));
            IssueTrackerMessageResult finalResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.DELETE, messageId);

            Optional<String> optionalReopenTransitionName = fieldAccessor.getString(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION).filter(StringUtils::isNotBlank);
            if (optionalReopenTransitionName.isPresent()) {
                fromStatus = toStatus;
                toStatus = "Reopen";
                Optional<String> reopenError = validateTransition(transitionHelper, initialIssueKey, optionalReopenTransitionName.get(), JiraTransitionHandler.TODO_STATUS_CATEGORY_KEY);
                reopenError.ifPresent(message -> transitionErrors.put(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, message));
                IssueTrackerMessageResult reopenResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.ADD, messageId);
                possibleSecondIssueKey = reopenResult.getUpdatedIssueKeys()
                                             .stream()
                                             .findFirst()
                                             .filter(secondIssueKey -> !StringUtils.equals(secondIssueKey, initialIssueKey));

                if (reopenError.isEmpty()) {
                    fromStatus = toStatus;
                    toStatus = "Resolve";
                    Optional<String> reResolveError = validateTransition(transitionHelper, initialIssueKey, resolveTransitionName, JiraTransitionHandler.DONE_STATUS_CATEGORY_KEY);
                    reResolveError.ifPresent(message -> transitionErrors.put(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, message));
                    finalResult = createAndSendMessage(jobId, fieldAccessor, ItemOperation.DELETE, messageId);
                }
            }

            if (transitionErrors.isEmpty()) {
                return finalResult;
            } else {
                throw new AlertFieldException(transitionErrors);
            }
        } catch (AlertFieldException fieldException) {
            safelyCleanUpIssue(issueService, initialIssueKey);
            throw fieldException;
        } catch (AlertException alertException) {
            logger.debug("Error testing Jira Cloud config", alertException);
            String errorMessage = String.format("There were problems transitioning the test issue from the %s status to the %s status: %s", fromStatus, toStatus, alertException.getMessage());
            possibleSecondIssueKey.ifPresent(key -> safelyCleanUpIssue(issueService, key));
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
        IssueTrackerMessageResult messageResult = ((JiraChannel) getDistributionChannel()).sendMessage(resolveIssueEvent);
        logger.debug("{} test message sent!", operation.name());
        return messageResult;
    }

    private Optional<String> validateTransition(JiraTransitionHandler jiraTransitionHelper, String issueKey, String transitionName, String statusCategoryKey) throws IntegrationException {
        Optional<TransitionComponent> transitionComponent = jiraTransitionHelper.retrieveIssueTransition(issueKey, transitionName);
        if (transitionComponent.isPresent()) {
            boolean isValidTransition = jiraTransitionHelper.doesTransitionToExpectedStatusCategory(transitionComponent.get(), statusCategoryKey);
            if (!isValidTransition) {
                return Optional.of("The provided transition would not result in an allowed status category.");
            }
        } else {
            return Optional.of("The provided transition is not possible from the issue state that it would transition from.");
        }
        return Optional.empty();
    }

    private void safelyCleanUpIssue(IssueService issueService, String issueKey) {
        try {
            issueService.deleteIssue(issueKey);
        } catch (IntegrationException e) {
            logger.warn("There was a problem trying to delete a the Jira Cloud distribution test issue, {}: {}", issueKey, e);
        }
    }

}
