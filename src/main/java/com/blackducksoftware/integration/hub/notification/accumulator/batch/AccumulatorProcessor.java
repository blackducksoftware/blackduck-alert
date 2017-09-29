package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;

public class AccumulatorProcessor implements ItemProcessor<NotificationResults, NotificationResults> {

    @Override
    public NotificationResults process(final NotificationResults notificationData) throws Exception {
        return null;
    }
}
