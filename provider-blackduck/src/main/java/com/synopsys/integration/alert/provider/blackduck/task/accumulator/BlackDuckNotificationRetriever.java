/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.util.List;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckNotificationRetriever {
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int INITIAL_PAGE_OFFSET = 0;

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;

    public BlackDuckNotificationRetriever(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
    }

    public StatefulAlertPage<NotificationView, IntegrationException> retrievePageOfFilteredNotifications(DateRange dateRange, List<String> types) throws IntegrationException {
        BlackDuckMultipleRequest<NotificationView> spec = createNotificationsRequest(dateRange, types);
        NotificationPageRetriever notificationRetriever = new NotificationPageRetriever(spec);
        AlertPagedDetails<NotificationView> firstPage = notificationRetriever.retrievePage(INITIAL_PAGE_OFFSET, DEFAULT_PAGE_SIZE);
        return new StatefulAlertPage<>(firstPage, notificationRetriever);
    }

    private BlackDuckPageResponse<NotificationView> retrievePageOfFilteredNotifications(BlackDuckMultipleRequest<NotificationView> spec) throws IntegrationException {
        return blackDuckApiClient.getPageResponse(spec);
    }

    private BlackDuckMultipleRequest<NotificationView> createNotificationsRequest(DateRange dateRange, List<String> notificationTypesToInclude) throws IntegrationException {
        NotificationEditor notificationEditor = new NotificationEditor(dateRange.getStart(), dateRange.getEnd(), notificationTypesToInclude);
        BlackDuckMultipleRequest<NotificationView> spec = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .apply(notificationEditor)
                                                              .buildBlackDuckRequest(apiDiscovery.metaNotificationsLink());
        return spec;
    }

    private class NotificationPageRetriever implements PageRetriever<NotificationView, IntegrationException> {
        private final BlackDuckMultipleRequest<NotificationView> spec;

        public NotificationPageRetriever(BlackDuckMultipleRequest<NotificationView> spec) {
            this.spec = spec;
        }

        @Override
        public AlertPagedDetails<NotificationView> retrieveNextPage(int currentOffset, int currentLimit) throws IntegrationException {
            int newOffset = currentOffset + currentLimit;
            return retrievePage(newOffset, currentLimit);
        }

        @Override
        public AlertPagedDetails<NotificationView> retrievePage(int currentOffset, int currentLimit) throws IntegrationException {
            BlackDuckMultipleRequest<NotificationView> pageSpec = new BlackDuckRequestBuilder(spec)
                                                                      .setLimitAndOffset(currentLimit, currentOffset)
                                                                      .buildBlackDuckRequest(spec.getUrlResponse());
            BlackDuckPageResponse<NotificationView> notificationPage = retrievePageOfFilteredNotifications(pageSpec);
            return new AlertPagedDetails<>(notificationPage.getTotalCount(), currentOffset, currentLimit, notificationPage.getItems());
        }
    }

}
