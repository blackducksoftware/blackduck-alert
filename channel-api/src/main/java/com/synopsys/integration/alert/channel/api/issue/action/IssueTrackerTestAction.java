/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.action;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.channel.api.issue.IssueTrackerChannel;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueTrackerTestAction<D extends DistributionJobDetailsModel, T extends Serializable> implements ChannelDistributionTestAction {
    private final IssueTrackerChannel<D, T> issueTrackerChannel;
    private final IssueTrackerTestActionMessageCreator messageCreator;

    public IssueTrackerTestAction(IssueTrackerChannel<D, T> issueTrackerChannel, IssueTrackerTestActionMessageCreator messageCreator) {
        this.issueTrackerChannel = issueTrackerChannel;
        this.messageCreator = messageCreator;
    }

    @Override
    public MessageResult testConfig(DistributionJobModel testJobModel, @Nullable String customTopic, @Nullable String customMessage, @Nullable String destination) throws IntegrationException {
        ProviderMessageHolder providerMessageHolder;
        if (null != customTopic || null != customMessage) {
            String topicString = Optional.ofNullable(customTopic).orElse("Alert Test Topic");
            String messageString = Optional.ofNullable(customMessage).orElse("Alert Test Message");

            SimpleMessage simpleMessage = SimpleMessage.original(messageCreator.createTestMessageProviderDetails(), topicString, messageString, List.of());
            providerMessageHolder = new ProviderMessageHolder(List.of(), List.of(simpleMessage));
        } else {
            ProjectMessage createMessage = messageCreator.createComponentConcernProjectMessage(ItemOperation.ADD);
            ProjectMessage resolveMessage = messageCreator.createComponentConcernProjectMessage(ItemOperation.DELETE);
            providerMessageHolder = new ProviderMessageHolder(List.of(createMessage, resolveMessage, createMessage), List.of());
        }
        return issueTrackerChannel.distributeMessages((D) testJobModel.getDistributionJobDetails(), providerMessageHolder);
    }

}
