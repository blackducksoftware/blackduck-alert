/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api.mock;

import java.util.function.Function;

import com.blackduck.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedNotificationRepository extends MockRepositoryContainer<Long, AuditFailedNotificationEntity> implements AuditFailedNotificationRepository {

    public MockAuditFailedNotificationRepository(Function<AuditFailedNotificationEntity, Long> idGenerator) {
        super(idGenerator);
    }
}
