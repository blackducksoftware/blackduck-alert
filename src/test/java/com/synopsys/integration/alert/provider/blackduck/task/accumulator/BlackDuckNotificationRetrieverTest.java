package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckNotificationRetriever.BlackDuckNotificationPage;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckNotificationRetrieverTest {
    @Test
    public void retrievePageOfFilteredNotificationsTest() throws IntegrationException {
        ProjectNotificationView projectNotificationView = new ProjectNotificationView();
        BlackDuckPageResponse<NotificationView> pageResponse = new BlackDuckPageResponse<>(1, List.of(projectNotificationView));
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.doReturn(pageResponse)
            .when(blackDuckApiClient)
            .getPageResponse(Mockito.any(BlackDuckRequestBuilder.class), Mockito.eq(NotificationView.class), Mockito.any());

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateRangeCreator();

        BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(new BlackDuckRequestFactory(), blackDuckApiClient);
        BlackDuckNotificationPage notificationPage = notificationRetriever.retrievePageOfFilteredNotifications(dateRangeCreator.retrieveNextSearchDateRange(), List.of());
        assertEquals(pageResponse.getItems(), notificationPage.getCurrentNotifications());
    }

    private BlackDuckAccumulatorSearchDateManager createDateRangeCreator() {
        ProviderTaskPropertiesAccessor taskPropertiesAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        Mockito.when(taskPropertiesAccessor.getTaskProperty(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        return new BlackDuckAccumulatorSearchDateManager(taskPropertiesAccessor, 0L, "Task");
    }

}
