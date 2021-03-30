package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;

public class BlackDuckNotificationRetrieverTest {
    private static final String NON_EMPTY_RESULT_ERROR_MESSAGE = "Expected notification views to be empty";

    @Test
    public void retrieveFilteredNotificationsTest() throws Exception {
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        NotificationView notificationView = new NotificationView();
        notificationView.setCreatedAt(new Date());
        notificationView.setContentType("content_type");
        notificationView.setType(NotificationType.RULE_VIOLATION);
        List<NotificationView> notificationViewList = List.of(notificationView);

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(mockedBlackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://localhost:443/alert"));
        Mockito.when(mockedBlackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(mockedBlackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getFilteredNotifications(Mockito.any(), Mockito.any(), Mockito.anyList());

        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateRangeCreator();
        DateRange dateRange = dateRangeCreator.retrieveNextSearchDateRange();

        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(mockedBlackDuckProperties);
        List<NotificationView> notificationViews = notificationRetriever.retrieveFilteredNotifications(dateRange, List.of());
        assertFalse(notificationViews.isEmpty());
    }

    @Test
    public void retrieveFilteredNotificationsNoNotificationsTest() throws Exception {
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        List<NotificationView> notificationViewList = List.of();

        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.doReturn(Optional.of(blackDuckHttpClient)).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());
        Mockito.doReturn(blackDuckServicesFactory).when(mockedBlackDuckProperties).createBlackDuckServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(blackDuckServicesFactory).createNotificationService();
        Mockito.doReturn(notificationViewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateRangeCreator();
        DateRange dateRange = dateRangeCreator.retrieveNextSearchDateRange();
        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(mockedBlackDuckProperties);

        List<NotificationView> notificationViews = notificationRetriever.retrieveFilteredNotifications(dateRange, List.of());
        assertTrue(notificationViews.isEmpty(), NON_EMPTY_RESULT_ERROR_MESSAGE);
    }

    @Test
    public void retrieveFilteredNotificationsMissingRestConnectionTest() {
        BlackDuckProperties mockedBlackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        Mockito.doReturn(Optional.empty()).when(mockedBlackDuckProperties).createBlackDuckHttpClientAndLogErrors(Mockito.any());

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateRangeCreator();
        DateRange dateRange = dateRangeCreator.retrieveNextSearchDateRange();
        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(mockedBlackDuckProperties);

        List<NotificationView> notificationViews = notificationRetriever.retrieveFilteredNotifications(dateRange, List.of());
        assertTrue(notificationViews.isEmpty(), NON_EMPTY_RESULT_ERROR_MESSAGE);
    }

    private BlackDuckAccumulatorSearchDateManager createDateRangeCreator() {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        Mockito.when(taskPropertiesAccessor.getTaskProperty(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        return new BlackDuckAccumulatorSearchDateManager(taskPropertiesAccessor, 0L, "Task");
    }

}
