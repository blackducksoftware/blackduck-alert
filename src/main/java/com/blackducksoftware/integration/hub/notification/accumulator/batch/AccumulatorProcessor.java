package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;

public class AccumulatorProcessor implements ItemProcessor<NotificationResults, DBStoreEvent> {
    private final NotificationAccumulatorProcessor notificationAccumulatorProcessor;

    public AccumulatorProcessor(final NotificationAccumulatorProcessor notificationAccumulatorProcessor) {
        this.notificationAccumulatorProcessor = notificationAccumulatorProcessor;
    }

    @Override
    public DBStoreEvent process(final NotificationResults notificationData) throws Exception {
        final DBStoreEvent storeEvent = notificationAccumulatorProcessor.process(notificationData.getNotificationContentItems());
        return storeEvent;
    }
}
