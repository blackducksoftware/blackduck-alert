package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.notification.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;

public class DigestItemProcessor implements ItemProcessor<List<NotificationEntity>, List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);

    @Override
    public List<AbstractChannelEvent> process(final List<NotificationEntity> notificationData) throws Exception {
        logger.info("Notification Entity Count: {}", notificationData.size());

        final List<AbstractChannelEvent> events = processNotifications(notificationData);

        if (events.isEmpty()) {
            return null;
        } else {
            return events;
        }
    }

    private List<AbstractChannelEvent> processNotifications(final List<NotificationEntity> notificationList) {

        if (notificationList == null) {
            return new ArrayList<>(0);
        } else {
            final List<AbstractChannelEvent> events = new ArrayList<>(notificationList.size());
            notificationList.forEach(notification -> {
                events.add(new EmailEvent(notification));
            });
            return events;
        }
    }
}
