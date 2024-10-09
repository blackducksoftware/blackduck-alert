/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

public abstract class ProviderMessageExtractor<T extends NotificationContentComponent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationType notificationType;
    private final Class<T> notificationContentClass;

    protected ProviderMessageExtractor(NotificationType notificationType, Class<T> notificationContentClass) {
        this.notificationType = notificationType;
        this.notificationContentClass = notificationContentClass;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public final ProcessedProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper) {
        if (!notificationContentClass.isAssignableFrom(notificationContentWrapper.getNotificationContentClass())) {
            logger.error("The notification type provided is incompatible with this extractor: {}", notificationContentWrapper.extractNotificationType());
            return ProcessedProviderMessageHolder.empty();
        }

        T stronglyTypedContent = notificationContentClass.cast(notificationContentWrapper.getNotificationContent());
        ProviderMessageHolder extractedMessages = extract(notificationContentWrapper, stronglyTypedContent);
        return toProcessedProviderMessageHolder(notificationContentWrapper.getNotificationId(), extractedMessages);
    }

    protected abstract ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, T notificationContent);

    private ProcessedProviderMessageHolder toProcessedProviderMessageHolder(Long notificationId, ProviderMessageHolder extractedMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages = extractProcessedProviderMessage(notificationId, extractedMessages.getProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages = extractProcessedProviderMessage(notificationId, extractedMessages.getSimpleMessages());
        return new ProcessedProviderMessageHolder(processedProjectMessages, processedSimpleMessages);
    }

    private <U extends ProviderMessage<U>> List<ProcessedProviderMessage<U>> extractProcessedProviderMessage(Long notificationId, List<U> providerMessages) {
        return providerMessages
                   .stream()
                   .map(providerMessage -> ProcessedProviderMessage.singleSource(notificationId, providerMessage))
                   .collect(Collectors.toList());
    }

}
