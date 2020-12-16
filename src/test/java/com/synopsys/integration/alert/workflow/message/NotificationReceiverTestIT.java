package com.synopsys.integration.alert.workflow.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

//TODO: This class depends on AlertIntegrationTest which cannot be moved into test-common yet due to it's dependencies.
//  Move this class into the workflow subproject once the dependencies are resolved
public class NotificationReceiverTestIT extends AlertIntegrationTest {

    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;
    @Autowired
    private NotificationReceiver notificationReceiver;

    @AfterEach
    public void cleanUpDB() {
        notificationContentRepository.flush();
        notificationContentRepository.deleteAllInBatch();
    }

    @Test
    public void testHandleEventNotProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(1L, false));
        notificationContent.add(createAlertNotificationModel(2L, false));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        notificationReceiver.handleEvent(new NotificationReceivedEvent());

        testAlertNotificationModels(savedModels);
    }

    @Test
    public void testHandleEventProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(1L, true));
        notificationContent.add(createAlertNotificationModel(2L, true));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);
        assertEquals(0, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed().getModels().size());

        notificationReceiver.handleEvent(new NotificationReceivedEvent());

        testAlertNotificationModels(savedModels);
    }

    @Test
    public void testHandleEventMixedProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(1L, true));
        notificationContent.add(createAlertNotificationModel(2L, false));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        notificationReceiver.handleEvent(new NotificationReceivedEvent());

        testAlertNotificationModels(savedModels);
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed) {
        return new AlertNotificationModel(id, 1L, "provider", "providerConfigName", "notificationType", "{content: \"content is here...\"}", DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), processed);
    }

    private void testAlertNotificationModels(List<AlertNotificationModel> models) {
        PageRequest pageRequest = defaultNotificationAccessor.getPageRequestForNotifications(0, models.size(), null, null);
        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.findAll(pageRequest, false).getContent();

        assertModelsAreProcessed(alertNotificationModels);
        assertEquals(0, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed().getModels().size());
    }

    private void assertModelsAreProcessed(List<AlertNotificationModel> notificationModels) {
        for (AlertNotificationModel notificationModel : notificationModels) {
            assertTrue(notificationModel.getProcessed());
        }
    }
}
