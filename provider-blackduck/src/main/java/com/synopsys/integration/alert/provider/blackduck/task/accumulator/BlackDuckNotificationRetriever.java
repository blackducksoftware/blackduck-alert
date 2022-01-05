/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.util.List;
import java.util.function.Predicate;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.blackduck.service.request.NotificationEditor;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckNotificationRetriever {
    public static final String PAGE_SORT_FIELD = "notification.createdOn";
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int INITIAL_PAGE_OFFSET = 0;

    // (offset + limit) < total
    // Ex: offset = 0; limit = 10; total = 10; (0 + 10) < 10 == false; no next page
    public static final Predicate<AlertPagedDetails> HAS_NEXT_PAGE = page -> (page.getCurrentPage() + page.getPageSize()) < page.getTotalPages();

    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;

    public BlackDuckNotificationRetriever(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
    }

    public StatefulAlertPage<NotificationUserView, IntegrationException> retrievePageOfFilteredNotifications(DateRange dateRange, List<String> types) throws IntegrationException {
        BlackDuckMultipleRequest<NotificationUserView> spec = createNotificationsRequest(dateRange, types);
        NotificationPageRetriever notificationRetriever = new NotificationPageRetriever(spec);
        AlertPagedDetails<NotificationUserView> firstPage = notificationRetriever.retrievePage(INITIAL_PAGE_OFFSET, DEFAULT_PAGE_SIZE);
        return new StatefulAlertPage<>(firstPage, notificationRetriever, HAS_NEXT_PAGE);
    }

    private BlackDuckPageResponse<NotificationUserView> retrievePageOfFilteredNotifications(BlackDuckMultipleRequest<NotificationUserView> spec) throws IntegrationException {
        return blackDuckApiClient.getPageResponse(spec);
    }

    private BlackDuckMultipleRequest<NotificationUserView> createNotificationsRequest(DateRange dateRange, List<String> notificationTypesToInclude) throws IntegrationException {
        UserView currentUser = blackDuckApiClient.getResponse(apiDiscovery.metaCurrentUserLink());
        UrlMultipleResponses<NotificationUserView> currentUserNotificationsUrl = currentUser.metaNotificationsLink();
        NotificationEditor notificationEditor = new NotificationEditor(dateRange.getStart(), dateRange.getEnd(), notificationTypesToInclude);
        BlackDuckMultipleRequest<NotificationUserView> spec = new BlackDuckRequestBuilder()
            .commonGet()
            .apply(notificationEditor)
            .addQueryParameter("sort", String.format("%s asc", PAGE_SORT_FIELD))
            .buildBlackDuckRequest(currentUserNotificationsUrl);
        return spec;
    }

    private class NotificationPageRetriever implements PageRetriever<NotificationUserView, IntegrationException> {
        private final BlackDuckMultipleRequest<NotificationUserView> spec;

        public NotificationPageRetriever(BlackDuckMultipleRequest<NotificationUserView> spec) {
            this.spec = spec;
        }

        @Override
        public AlertPagedDetails<NotificationUserView> retrieveNextPage(int currentOffset, int currentLimit) throws IntegrationException {
            int newOffset = currentOffset + currentLimit;
            return retrievePage(newOffset, currentLimit);
        }

        @Override
        public AlertPagedDetails<NotificationUserView> retrievePage(int currentOffset, int currentLimit) throws IntegrationException {
            BlackDuckMultipleRequest<NotificationUserView> pageSpec = new BlackDuckRequestBuilder(spec)
                .setLimitAndOffset(currentLimit, currentOffset)
                .buildBlackDuckRequest(spec.getUrlResponse());
            BlackDuckPageResponse<NotificationUserView> notificationPage = retrievePageOfFilteredNotifications(pageSpec);
            return new AlertPagedDetails<>(notificationPage.getTotalCount(), currentOffset, currentLimit, notificationPage.getItems());
        }

    }

}
