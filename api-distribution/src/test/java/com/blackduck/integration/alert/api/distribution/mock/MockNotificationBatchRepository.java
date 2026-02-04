/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.mock;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blackduck.integration.alert.database.notification.NotificationBatchEntity;
import com.blackduck.integration.alert.database.notification.NotificationBatchPK;
import com.blackduck.integration.alert.database.notification.NotificationBatchRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockNotificationBatchRepository extends MockRepositoryContainer <NotificationBatchPK, NotificationBatchEntity> implements NotificationBatchRepository {

    public static NotificationBatchPK generateNotificationBatchKey(NotificationBatchEntity entity) {
        NotificationBatchPK key = new NotificationBatchPK();
        key.setProviderId(entity.getProviderId());
        key.setBatchId(entity.getBatchId());
        key.setNotificationId(entity.getNotificationId());
        return key;
    }
    public MockNotificationBatchRepository() {
        super(MockNotificationBatchRepository::generateNotificationBatchKey);
    }

    @Override
    public Page<UUID> findUniqueBatchIdsForProviderWhereNotificationsNotProcessed(final Long providerId, final Pageable pageable) {
        return null;
    }
}
