package com.synopsys.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
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
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

class NotificationReceivedEventHandlerTest {
    private final Gson gson = new Gson();
    private final BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);

    @Test
    void handleEventTest() throws IOException {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationProcessor notificationProcessor = mockNotificationProcessor(notificationAccessor);
        EventManager eventManager = mockEventManager();
        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationProcessor, eventManager);

        try {
            eventHandler.handle(new NotificationReceivedEvent());
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }

    @Test
    void handleEventProcessingInterruptedTest() throws IOException {
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doAnswer(invocation -> {
            throw new InterruptedException("Test: exception for thread");
        }).when(notificationAccessor).getFirstPageOfNotificationsNotProcessed(Mockito.anyInt());
        NotificationProcessor notificationProcessor = mockNotificationProcessor(notificationAccessor);
        EventManager eventManager = mockEventManager();
        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationProcessor, eventManager);

        try {
            eventHandler.handle(new NotificationReceivedEvent());
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }

    @Test
    void handleEventProcessingExceptionTest() throws IOException {
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doAnswer((invocation) -> {
            throw new ExecutionException(new RuntimeException("Test: exception for thread"));
        }).when(notificationAccessor).getFirstPageOfNotificationsNotProcessed(Mockito.anyInt());
        NotificationProcessor notificationProcessor = mockNotificationProcessor(notificationAccessor);
        EventManager eventManager = mockEventManager();
        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationProcessor, eventManager);

        try {
            eventHandler.handle(new NotificationReceivedEvent());
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
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

    private NotificationProcessor mockNotificationProcessor(NotificationAccessor notificationAccessor) {
        NotificationDetailExtractionDelegator detailExtractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        Predicate<AlertPagedDetails> hasNextPage = page -> page.getCurrentPage() < (page.getTotalPages() - 1);
        StatefulAlertPage<FilteredJobNotificationWrapper, RuntimeException> statefulAlertPage = new StatefulAlertPage(AlertPagedDetails.emptyPage(), Mockito.mock(PageRetriever.class), hasNextPage);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyList())).thenReturn(statefulAlertPage);
        return new NotificationProcessor(detailExtractionDelegator, jobNotificationMapper, null, null, List.of(), notificationAccessor);
    }

    private EventManager mockEventManager() {
        JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        Gson gson = new Gson();

        return new EventManager(gson, jmsTemplate);
    }

}
