package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;

public class DigestItemProcessor implements ItemProcessor<List<NotificationEntity>, Object> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);

    @Override
    public Object process(final List<NotificationEntity> notificationData) throws Exception {
        logger.info("Notification Entity Count: {}", notificationData.size());
        return null;
    }
}
