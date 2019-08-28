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
package com.synopsys.integration.alert.channel.jira.actions;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.JiraChannel;
import com.synopsys.integration.alert.common.action.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

@Component
public class JiraDistributionTestAction extends ChannelDistributionTestAction {
    private final Logger logger = LoggerFactory.getLogger(JiraDistributionTestAction.class);

    @Autowired
    public JiraDistributionTestAction(final JiraChannel jiraChannel) {
        super(jiraChannel);
    }

    @Override
    public String testConfig(String jobId, String destination, FieldAccessor fieldAccessor) throws IntegrationException {
        String messageId = UUID.randomUUID().toString();

        logger.debug("Sending initial ADD test message...");
        final DistributionEvent createIssueEvent = createChannelTestEvent(jobId, fieldAccessor, ItemOperation.ADD, messageId);
        final String initialTestResult = getDistributionChannel().sendMessage(createIssueEvent);
        logger.debug("Initial ADD test message sent!");

        String fromStatus = "Initial";
        String toStatus = "Resolve";
        try {
            logger.debug("Sending DELETE test message...");
            final DistributionEvent resolveIssueEvent = createChannelTestEvent(jobId, fieldAccessor, ItemOperation.DELETE, messageId);
            getDistributionChannel().sendMessage(resolveIssueEvent);
            logger.debug("DELETE test message sent!");

            fromStatus = toStatus;
            toStatus = "Reopen";
            logger.debug("Sending additional ADD test message...");
            final DistributionEvent reOpenIssueEvent = createChannelTestEvent(jobId, fieldAccessor, ItemOperation.ADD, messageId);
            getDistributionChannel().sendMessage(reOpenIssueEvent);
            logger.debug("Additional ADD test message sent!");

            fromStatus = toStatus;
            toStatus = "Resolve";
            logger.debug("Sending additional DELETE test message...");
            final DistributionEvent reResolveIssueEvent = createChannelTestEvent(jobId, fieldAccessor, ItemOperation.DELETE, messageId);
            String reResolveResult = getDistributionChannel().sendMessage(reResolveIssueEvent);
            logger.debug("Additional DELETE test message sent!");

            return reResolveResult;
        } catch (AlertException e) {
            // Any specific exceptions will have already been thrown by the initial message attempt, so we should only see AlertExceptions at this point.
            logger.debug("Error testing Jira Cloud config", e);
            String errorMessage = String.format("The initial test succeeded, but there were problems transitioning the test issue from the %s status to the %s status. | Initial Result: %s | Transition Result: %s",
                fromStatus, toStatus, initialTestResult, e.getMessage());
            throw new AlertException(errorMessage);
        }
    }

    public DistributionEvent createChannelTestEvent(final String jobId, final FieldAccessor fieldAccessor, ItemOperation operation, String messageId) throws AlertException {
        final ProviderMessageContent messageContent = createTestNotificationContent(fieldAccessor, operation, messageId);

        final String channelName = fieldAccessor.getStringOrEmpty(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        final String providerName = fieldAccessor.getStringOrEmpty(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        final String formatType = fieldAccessor.getStringOrEmpty(ProviderDistributionUIConfig.KEY_FORMAT_TYPE);

        return new DistributionEvent(jobId, channelName, RestConstants.formatDate(new Date()), providerName, formatType, MessageContentGroup.singleton(messageContent), fieldAccessor);
    }
}
