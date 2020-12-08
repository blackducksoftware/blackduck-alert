package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.workflow.message.mocks.MockNotificationAccessor;

public class NotificationReceiverTest {
    private NotificationAccessor notificationAccessor;
    private final Gson gson = new Gson();

    //TODO
    // 1 - Test normal run, get page of notifications, run through the loop, find another page, and quit
    // 2 - test numPagesProcessed if the loop runs 1000 times it quits like expected
    @Test
    public void handleEventTest() {
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(List.of(createAlertNotificationModel()));
        //NotificationProcessor notificationProcessor = Mockio.mock();

        //NotificationReceiver notificationReceiver = new NotificationReceiver(gson, notificationAccessor, );
    }

    private AlertNotificationModel createAlertNotificationModel() {
        Long id = 1L;
        Long providerConfigId = 2L;
        String provider = "provider-test";
        String notificationType = "notificationType-test";
        String content = "content";
        String fieldValue = "test-channel.common.name-value";

        return new AlertNotificationModel(id, providerConfigId, provider, fieldValue, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);
    }
}
