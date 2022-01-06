/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract;

import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public final class ProviderMessageExtractionDelegator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EnumMap<NotificationType, ProviderMessageExtractor> notificationTypeToExtractor;

    @Autowired
    public ProviderMessageExtractionDelegator(List<ProviderMessageExtractor> providerMessageExtractors) {
        this.notificationTypeToExtractor = initializeExtractorMap(providerMessageExtractors);
    }

    public ProcessedProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper) {
        String notificationTypeString = notificationContentWrapper.extractNotificationType();
        NotificationType filteredNotificationType = EnumUtils.getEnum(NotificationType.class, notificationTypeString);
        if (null == filteredNotificationType) {
            logger.warn("Notification did not match any existing notification type: {}", notificationTypeString);
            return ProcessedProviderMessageHolder.empty();
        }

        ProviderMessageExtractor providerMessageExtractor = notificationTypeToExtractor.get(filteredNotificationType);
        if (null == providerMessageExtractor) {
            logger.warn("No matching extractor for notification type: {}", notificationTypeString);
            return ProcessedProviderMessageHolder.empty();
        }

        return providerMessageExtractor.extract(notificationContentWrapper);
    }

    private EnumMap<NotificationType, ProviderMessageExtractor> initializeExtractorMap(List<ProviderMessageExtractor> providerMessageExtractors) {
        return providerMessageExtractors
                   .stream()
                   .collect(DataStructureUtils.toEnumMap(ProviderMessageExtractor::getNotificationType, NotificationType.class));
    }

}
