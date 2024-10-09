package com.blackduck.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.provider.blackduck.task.accumulator.BlackDuckAccumulatorSearchDateManager;
import com.blackduck.integration.alert.provider.blackduck.task.accumulator.BlackDuckNotificationRetriever;
import com.blackduck.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.blackduck.integration.alert.api.processor.filter.StatefulAlertPage;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.view.NotificationUserView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectNotificationUserView;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

class BlackDuckNotificationRetrieverTest {
    private static final String THROWAWAY_SERVER = "https://someblackduckserver";

    @Test
    void retrievePageOfFilteredNotificationsTest() throws IntegrationException {
        ProjectNotificationUserView projectNotificationView = new ProjectNotificationUserView();
        BlackDuckPageResponse<NotificationUserView> pageResponse = new BlackDuckPageResponse<>(1, List.of(projectNotificationView));

        UrlMultipleResponses<NotificationUserView> currentUserNotificationsUrl = new UrlMultipleResponses<>(new HttpUrl(THROWAWAY_SERVER), NotificationUserView.class);
        UserView apiUser = Mockito.mock(UserView.class);
        Mockito.doReturn(currentUserNotificationsUrl)
            .when(apiUser)
            .metaNotificationsLink();

        UrlSingleResponse<UserView> currentUserUrl = new UrlSingleResponse<>(new HttpUrl(THROWAWAY_SERVER), UserView.class);
        ApiDiscovery apiDiscovery = Mockito.mock(ApiDiscovery.class);
        Mockito.doReturn(currentUserUrl)
            .when(apiDiscovery)
            .metaCurrentUserLink();

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.doReturn(pageResponse)
            .when(blackDuckApiClient)
            .getPageResponse(Mockito.any(BlackDuckMultipleRequest.class));
        Mockito.doReturn(apiUser)
            .when(blackDuckApiClient)
            .getResponse(currentUserUrl);

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateRangeCreator();

        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(blackDuckApiClient, apiDiscovery);
        StatefulAlertPage<NotificationUserView, IntegrationException> notificationPage = notificationRetriever.retrievePageOfFilteredNotifications(dateRangeCreator.retrieveNextSearchDateRange(), List.of());
        assertEquals(pageResponse.getItems(), notificationPage.getCurrentModels());
    }

    private BlackDuckAccumulatorSearchDateManager createDateRangeCreator() {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        Mockito.when(taskPropertiesAccessor.getTaskProperty(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        return new BlackDuckAccumulatorSearchDateManager(taskPropertiesAccessor, 0L, "Task");
    }

}
