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
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.filter.NextPageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
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

    public StatefulAlertPage<NotificationView, IntegrationException> retrievePageOfFilteredNotifications(DateRange dateRange, List<String> types) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = createNotificationRequestBuilder(dateRange, types);
        BlackDuckPageDefinition pageDefinition = new BlackDuckPageDefinition(DEFAULT_PAGE_SIZE, INITIAL_PAGE_OFFSET);

        BlackDuckPageResponse<NotificationView> notificationPage = retrievePageOfFilteredNotifications(requestBuilder, pageDefinition);
        AlertPagedDetails firstPage = new AlertPagedDetails(INITIAL_PAGE_OFFSET, DEFAULT_PAGE_SIZE, notificationPage.getTotalCount(), notificationPage.getItems());

        NextNotificationPageRetriever notificationRetriever = new NextNotificationPageRetriever(requestBuilder);
        return new StatefulAlertPage(firstPage, notificationRetriever);
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

    private class NextNotificationPageRetriever implements NextPageRetriever<NotificationView, IntegrationException> {

        private final BlackDuckRequestBuilder blackDuckRequestBuilder;

        public NextNotificationPageRetriever(BlackDuckRequestBuilder blackDuckRequestBuilder) {
            this.blackDuckRequestBuilder = blackDuckRequestBuilder;
        }

        @Override
        public AlertPagedDetails<NotificationView> retrieveNextPage(int currentOffset, int currentLimit) throws IntegrationException {
            int newOffset = currentOffset + currentLimit;
            BlackDuckPageDefinition pageDefinition = new BlackDuckPageDefinition(currentLimit, newOffset);
            BlackDuckPageResponse<NotificationView> notificationPage = retrievePageOfFilteredNotifications(blackDuckRequestBuilder, pageDefinition);
            return new AlertPagedDetails(newOffset, currentLimit, notificationPage.getTotalCount(), notificationPage.getItems());
        }
    }

}
