package com.blackducksoftware.integration.hub.notification.batch.digest;

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

public abstract class DigestItemReader implements ItemReader<List<NotificationEntity>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationRepository notificationRepository;
    private boolean hasRead;

    public DigestItemReader(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        hasRead = false;
    }

    @Override
    public List<NotificationEntity> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (hasRead) {
            return null;
        } else {
            logger.debug("Digest Item Reader called...");
            final DateRange dateRange = getDateRange();
            final Date startDate = dateRange.getStart();
            final Date endDate = dateRange.getEnd();
            final List<NotificationEntity> entityList = notificationRepository.findByCreatedAtBetween(startDate, endDate);
            hasRead = true;
            if (entityList.isEmpty()) {
                return null;
            } else {
                return entityList;
            }
        }
    }

    public abstract DateRange getDateRange();

}
