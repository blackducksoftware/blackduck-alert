/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckNotificationRetriever {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckProperties blackDuckProperties;

    public BlackDuckNotificationRetriever(BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    public List<NotificationView> retrieveFilteredNotifications(DateRange dateRange, Collection<NotificationType> types) {
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(optionalBlackDuckHttpClient.get(), new Slf4jIntLogger(logger));
            NotificationService notificationService = blackDuckServicesFactory.createNotificationService();
            List<String> notificationTypeNames = extractNotificationTypeNames(types);
            return retrieveFilteredNotifications(notificationService, dateRange, notificationTypeNames);
        }
        return List.of();
    }

    private List<NotificationView> retrieveFilteredNotifications(NotificationService notificationService, DateRange dateRange, List<String> notificationTypeNames) {
        OffsetDateTime startDate = dateRange.getStart();
        OffsetDateTime endDate = dateRange.getEnd();
        logger.info("Accumulating Notifications Between {} and {} ", DateUtils.formatDate(startDate, RestConstants.JSON_DATE_FORMAT), DateUtils.formatDate(endDate, RestConstants.JSON_DATE_FORMAT));
        try {
            List<NotificationView> notificationViews = notificationService.getFilteredNotifications(Date.from(startDate.toInstant()), Date.from(endDate.toInstant()), notificationTypeNames);
            logger.debug("Read Notification Count: {}", notificationViews.size());
            return notificationViews;
        } catch (Exception e) {
            logger.error("Error reading notifications", e);
        }
        return List.of();
    }

    private List<String> extractNotificationTypeNames(Collection<NotificationType> notificationTypes) {
        return notificationTypes
                   .stream()
                   .map(Enum::name)
                   .collect(Collectors.toList());
    }

}
