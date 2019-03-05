package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;

public interface BaseNotificationManager {
    AlertNotificationWrapper saveNotification(final AlertNotificationWrapper notification);

    List<AlertNotificationWrapper> findByIds(final List<Long> notificationIds);

    Optional<AlertNotificationWrapper> findById(final Long notificationId);

    List<AlertNotificationWrapper> findByCreatedAtBetween(final Date startDate, final Date endDate);

    List<AlertNotificationWrapper> findByCreatedAtBefore(final Date date);

    List<AlertNotificationWrapper> findByCreatedAtBeforeDayOffset(final int dayOffset);

    void deleteNotificationList(final List<AlertNotificationWrapper> notifications);

    void deleteNotification(final AlertNotificationWrapper notification);

}
