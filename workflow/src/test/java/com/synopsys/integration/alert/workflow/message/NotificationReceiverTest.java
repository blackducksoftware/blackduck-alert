package com.synopsys.integration.alert.workflow.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.filter.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.alert.workflow.message.mocks.MockNotificationAccessor;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

public class NotificationReceiverTest {
    private NotificationAccessor notificationAccessor;
    private final Gson gson = new Gson();
    private final BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);

    @Test
    public void handleEventTest() throws IOException {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);

        NotificationProcessor notificationProcessor = mockNotificationProcessor(alertNotificationModels);
        EventManager eventManager = mockEventManager();
        NotificationReceiver notificationReceiver = new NotificationReceiver(gson, notificationAccessor, notificationProcessor, eventManager);

        try {
            notificationReceiver.handleEvent(new NotificationReceivedEvent());
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }

    @Test
    public void getDestinationNameTest() {
        NotificationReceiver notificationReceiver = new NotificationReceiver(gson, null, null, null);

        assertEquals(NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE, notificationReceiver.getDestinationName());
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed) throws IOException {
        Long providerConfigId = 2L;
        String provider = "provider-test";
        String notificationType = "PROJECT_VERSION";
        String content = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
        String providerConfigName = "providerConfigName";

        return new AlertNotificationModel(id, providerConfigId, provider, providerConfigName, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), processed);
    }

    private NotificationProcessor mockNotificationProcessor(List<AlertNotificationModel> alertNotificationModels) {
        NotificationDetailExtractionDelegator detailExtractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        Predicate<AlertPagedDetails> hasNextPage = page -> page.getCurrentPage() < (page.getTotalPages() - 1);
        StatefulAlertPage<FilteredJobNotificationWrapper, RuntimeException> statefulAlertPage = new StatefulAlertPage(AlertPagedDetails.emptyPage(), Mockito.mock(PageRetriever.class), hasNextPage);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyList())).thenReturn(statefulAlertPage);
        notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        return new NotificationProcessor(detailExtractionDelegator, jobNotificationMapper, null, null, List.of(), notificationAccessor);
    }

    private EventManager mockEventManager() {
        JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));

        Gson gson = new Gson();
        String testEventJson = gson.toJson(new NotificationReceivedEvent());

        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.when(contentConverter.getJsonString(Mockito.any(AlertEvent.class))).thenReturn(testEventJson);

        return new EventManager(contentConverter, jmsTemplate);
    }

}
