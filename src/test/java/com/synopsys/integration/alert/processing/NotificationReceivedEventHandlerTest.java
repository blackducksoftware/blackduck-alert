package com.synopsys.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class NotificationReceivedEventHandlerTest {

    @Test
    void handleEventTest() throws IOException {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationMappingProcessor notificationMappingProcessor = Mockito.mock(NotificationMappingProcessor.class);
        EventManager eventManager = mockEventManager();
        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);

        try {
            eventHandler.handle(new NotificationReceivedEvent(2L));
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
        String contentId = String.format("content-id-%s", UUID.randomUUID());

        return new AlertNotificationModel(
            id,
            providerConfigId,
            provider,
            providerConfigName,
            notificationType,
            content,
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            processed,
            contentId
        );
    }

    private EventManager mockEventManager() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        Gson gson = BlackDuckServicesFactory.createDefaultGson();

        return new EventManager(gson, rabbitTemplate, new SyncTaskExecutor());
    }

}
