/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface NotificationAccessor {
    List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications);

    List<AlertNotificationModel> findByIds(List<Long> notificationIds);

    Optional<AlertNotificationModel> findById(Long notificationId);

    AlertPagedModel<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate, int pageNumber, int pageSize);

    List<AlertNotificationModel> findByCreatedAtBefore(OffsetDateTime date);

    List<AlertNotificationModel> findByCreatedAtBeforeDayOffset(int dayOffset);

    @Deprecated(since = "6.13.0")
    AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(int pageSize);

    AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(long providerConfigId, int pageSize);

    void setNotificationsProcessed(List<AlertNotificationModel> notifications);

    void setNotificationsProcessedById(Set<Long> notificationIds);

    int deleteNotificationsCreatedBefore(OffsetDateTime date);

    void deleteNotification(AlertNotificationModel notification);

    @Deprecated(since = "6.13.0")
    boolean hasMoreNotificationsToProcess();

    boolean hasMoreNotificationsToProcess(long providerConfigId);

    long countNotificationsByProviderAndType(long providerConfigId, String notificationType);

}
