/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.message.MessageContentProcessor;

public abstract class ProviderMessageContentCollector {
    private final Map<ProcessingType, MessageContentProcessor> messageContentProcessorMap;

    public ProviderMessageContentCollector(List<MessageContentProcessor> messageContentFormatters) {
        this.messageContentProcessorMap = DataStructureUtils.mapToValues(messageContentFormatters, MessageContentProcessor::getProcessingType);
    }

    public final List<MessageContentGroup> createMessageContentGroups(DistributionJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException {
        List<ProviderMessageContent> messages = createProviderMessageContents(job, cache, notifications);
        return messageContentProcessorMap.get(job.getProcessingType()).process(messages);
    }

    protected abstract List<ProviderMessageContent> createProviderMessageContents(DistributionJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException;

}
