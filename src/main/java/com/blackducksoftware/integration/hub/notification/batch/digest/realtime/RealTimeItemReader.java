package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;

public class RealTimeItemReader implements ItemReader<Object> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);
    private final NotificationRepository notificationRepository;

    public RealTimeItemReader(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        ZonedDateTime currentTime = ZonedDateTime.now();
        currentTime = currentTime.withZoneSameInstant(ZoneOffset.UTC);
        currentTime = currentTime.withSecond(0).withNano(0);
        final ZonedDateTime zonedEndDate = currentTime.minusMinutes(1);
        final ZonedDateTime zonedStartDate = currentTime.minusMinutes(2);
        final Date startDate = Date.from(zonedStartDate.toInstant());
        final Date endDate = Date.from(zonedEndDate.toInstant());
        final Iterable<NotificationEntity> allItemsList = notificationRepository.findAll();
        final List<NotificationEntity> entityList = notificationRepository.findByCreatedAtBetween(startDate, endDate);
        logger.info("All Notificaiton Count: {}", allItemsList);
        logger.info("Notification Entity Count: {}", entityList.size());
        return null;
    }
}
