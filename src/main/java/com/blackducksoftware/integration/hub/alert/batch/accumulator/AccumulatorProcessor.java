package com.blackducksoftware.integration.hub.alert.batch.accumulator;

import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;

public class AccumulatorProcessor implements ItemProcessor<NotificationResults, DBStoreEvent> {
    private final NotificationItemProcessor notificationAccumulatorProcessor;

    public AccumulatorProcessor(final NotificationItemProcessor notificationAccumulatorProcessor) {
        this.notificationAccumulatorProcessor = notificationAccumulatorProcessor;
    }

    @Override
    public DBStoreEvent process(final NotificationResults notificationData) throws Exception {
        final DBStoreEvent storeEvent = notificationAccumulatorProcessor.process(notificationData.getNotificationContentItems());
        return storeEvent;
    }
}
