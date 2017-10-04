package com.blackducksoftware.integration.hub.notification.datasource.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;

public interface NotificationRepository extends CrudRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByCreatedAtBetween(final Date startDate, final Date endDate);

}
