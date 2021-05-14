/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckNotificationRetriever {
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int INITIAL_PAGE_OFFSET = 0;

    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;

    public BlackDuckNotificationRetriever(BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery) {
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
    }

    public StatefulAlertPage<NotificationView, IntegrationException> retrievePageOfFilteredNotifications(DateRange dateRange, List<String> types) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(dateRange, types);
        NotificationPageRetriever notificationRetriever = new NotificationPageRetriever(requestBuilder);
        AlertPagedDetails<NotificationView> firstPage = notificationRetriever.retrievePage(INITIAL_PAGE_OFFSET, DEFAULT_PAGE_SIZE);
        return new StatefulAlertPage<>(firstPage, notificationRetriever);
    }

    private BlackDuckPageResponse<NotificationView> retrievePageOfFilteredNotifications(BlackDuckRequestBuilder requestBuilder, BlackDuckPageDefinition pageDefinition) throws IntegrationException {
        return blackDuckApiClient.getPageResponse(requestBuilder, NotificationView.class, pageDefinition);
    }

    private BlackDuckRequestBuilder createNotificationRequestBuilder(DateRange dateRange, List<String> notificationTypesToInclude) throws IntegrationException {
        UrlMultipleResponses<NotificationView> notificationsResponses = apiDiscovery.metaNotificationsLink();
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory
                                                              .createCommonGet()
                                                              .url(notificationsResponses.getUrl());

        NotificationEditor notificationEditor = new NotificationEditor(toDate(dateRange.getStart()), toDate(dateRange.getEnd()), notificationTypesToInclude);
        notificationEditor.edit(blackDuckRequestBuilder);

        return blackDuckRequestBuilder;
    }

    private Date toDate(OffsetDateTime offsetDateTime) {
        return Date.from(offsetDateTime.toInstant());
    }

    private class NotificationPageRetriever implements PageRetriever<NotificationView, IntegrationException> {
        private final BlackDuckRequestBuilder blackDuckRequestBuilder;

        public NotificationPageRetriever(BlackDuckRequestBuilder blackDuckRequestBuilder) {
            this.blackDuckRequestBuilder = blackDuckRequestBuilder;
        }

        @Override
        public AlertPagedDetails<NotificationView> retrieveNextPage(int currentOffset, int currentLimit) throws IntegrationException {
            int newOffset = currentOffset + currentLimit;
            return retrievePage(newOffset, currentLimit);
        }

        @Override
        public AlertPagedDetails<NotificationView> retrievePage(int currentOffset, int currentLimit) throws IntegrationException {
            BlackDuckPageDefinition pageDefinition = new BlackDuckPageDefinition(currentLimit, currentOffset);
            BlackDuckPageResponse<NotificationView> notificationPage = retrievePageOfFilteredNotifications(blackDuckRequestBuilder, pageDefinition);
            return new AlertPagedDetails<>(notificationPage.getTotalCount(), currentOffset, currentLimit, notificationPage.getItems());
        }

    }

}
