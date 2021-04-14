/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingFunction;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckNotificationRetriever {
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int INITIAL_PAGE_OFFSET = 0;

    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final BlackDuckApiClient blackDuckApiClient;

    public BlackDuckNotificationRetriever(BlackDuckRequestFactory blackDuckRequestFactory, BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public BlackDuckNotificationPage retrievePageOfFilteredNotifications(DateRange dateRange, List<String> types) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(dateRange, types);
        NotificationPageRetriever notificationRetriever = pageDef -> retrievePageOfFilteredNotifications(requestBuilder, pageDef);

        BlackDuckPageDefinition firstPageDef = new BlackDuckPageDefinition(DEFAULT_PAGE_SIZE, INITIAL_PAGE_OFFSET);
        BlackDuckPageResponse<NotificationView> firstPage = notificationRetriever.apply(firstPageDef);

        return new BlackDuckNotificationPage(firstPageDef.getOffset(), firstPageDef.getLimit(), firstPage.getTotalCount(), firstPage.getItems(), notificationRetriever);
    }

    private BlackDuckPageResponse<NotificationView> retrievePageOfFilteredNotifications(BlackDuckRequestBuilder requestBuilder, BlackDuckPageDefinition pageDefinition) throws IntegrationException {
        return blackDuckApiClient.getPageResponse(requestBuilder, NotificationView.class, pageDefinition);
    }

    private BlackDuckRequestBuilder createNotificationRequestBuilder(DateRange dateRange, List<String> notificationTypesToInclude) throws IntegrationException {
        HttpUrl requestUrl = blackDuckApiClient.getUrl(ApiDiscovery.NOTIFICATIONS_LINK);

        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startDateString = toDateString(sdf, dateRange.getStart());
        String endDateString = toDateString(sdf, dateRange.getEnd());

        BlackDuckRequestFilter notificationTypeFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", notificationTypesToInclude);
        return blackDuckRequestFactory
                   .createCommonGetRequestBuilder()
                   .url(requestUrl)
                   .addQueryParameter("startDate", startDateString)
                   .addQueryParameter("endDate", endDateString)
                   .addBlackDuckFilter(notificationTypeFilter);
    }

    private String toDateString(SimpleDateFormat sdf, OffsetDateTime offsetDateTime) {
        Date date = Date.from(offsetDateTime.toInstant());
        return sdf.format(date);
    }

    public static class BlackDuckNotificationPage {
        private final int currentOffset;
        private final int currentLimit;
        private final int currentTotal;
        private final List<NotificationView> currentNotifications;
        private final NotificationPageRetriever notificationPageRetriever;

        private BlackDuckNotificationPage(
            int currentOffset,
            int currentLimit,
            int currentTotal,
            List<NotificationView> currentNotifications,
            NotificationPageRetriever notificationPageRetriever
        ) {
            this.currentOffset = currentOffset;
            this.currentLimit = currentLimit;
            this.currentTotal = currentTotal;
            this.currentNotifications = currentNotifications;
            this.notificationPageRetriever = notificationPageRetriever;
        }

        public BlackDuckNotificationPage retrieveNextPage() throws IntegrationException {
            int newOffset = currentOffset + currentLimit;
            if (hasNextPage()) {
                BlackDuckPageDefinition pageDefinition = new BlackDuckPageDefinition(currentLimit, newOffset);
                BlackDuckPageResponse<NotificationView> notificationPage = notificationPageRetriever.apply(pageDefinition);
                return new BlackDuckNotificationPage(newOffset, currentLimit, notificationPage.getTotalCount(), notificationPage.getItems(), notificationPageRetriever);
            }
            return new BlackDuckNotificationPage(newOffset, currentLimit, 0, List.of(), notificationPageRetriever);
        }

        public List<NotificationView> getCurrentNotifications() {
            return currentNotifications;
        }

        public boolean isEmpty() {
            return currentTotal == 0;
        }

        private boolean hasNextPage() {
            return currentTotal == currentLimit;
        }

    }

    private interface NotificationPageRetriever extends ThrowingFunction<BlackDuckPageDefinition, BlackDuckPageResponse<NotificationView>, IntegrationException> {
    }

}
