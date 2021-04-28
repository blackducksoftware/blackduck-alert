/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class ProviderMessageDistributor {
    private static final String EVENT_CLASS_NAME = DistributionEvent.class.getSimpleName();
    private static final String DESTINATION_WRAPPER_CLASS_NAME = ChannelKey.class.getSimpleName();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuditAccessor auditAccessor;
    private final EventManager eventManager;

    @Autowired
    public ProviderMessageDistributor(AuditAccessor auditAccessor, EventManager eventManager) {
        this.auditAccessor = auditAccessor;
        this.eventManager = eventManager;
    }

    public void distribute(ProcessedNotificationDetails processedNotificationDetails, ProcessedProviderMessageHolder providerMessageHolder) {
        UUID jobId = processedNotificationDetails.getJobId();
        Long auditId = auditAccessor.findOrCreatePendingAuditEntryForJob(jobId, processedNotificationDetails.getNotificationIds());

        // FIXME update event handling to accept the new models
        List<ProjectMessage> projectMessages = extractProviderMessages(providerMessageHolder.getProcessedProjectMessages());
        List<SimpleMessage> simpleMessages = extractProviderMessages(providerMessageHolder.getProcessedSimpleMessages());

        ProviderMessageHolder tempProviderMessageHolder = new ProviderMessageHolder(projectMessages, simpleMessages);

        String channelName = processedNotificationDetails.getChannelName();
        ChannelKey destinationKey = ChannelKeys.getChannelKey(channelName);
        if (null != destinationKey) {
            DistributionEvent event = new DistributionEvent(destinationKey, jobId, auditId, tempProviderMessageHolder);
            logger.info("Sending {}. Event ID: {}. Job ID: {}. Destination: {}", EVENT_CLASS_NAME, event.getEventId(), jobId, destinationKey);
            eventManager.sendEvent(event);
        } else {
            logger.warn("Unable to send {}. No {} with the name {} exists", EVENT_CLASS_NAME, DESTINATION_WRAPPER_CLASS_NAME, channelName);
        }
    }

    private <T extends ProviderMessage<T>> List<T> extractProviderMessages(List<ProcessedProviderMessage<T>> processedMessages) {
        return processedMessages
                   .stream()
                   .map(ProcessedProviderMessage::getProviderMessage)
                   .collect(Collectors.toList());
    }

}
