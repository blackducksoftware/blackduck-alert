package com.blackduck.integration.alert.database.job.api.mock;

import java.util.function.Function;

import com.blackduck.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedNotificationRepository extends MockRepositoryContainer<Long, AuditFailedNotificationEntity> implements AuditFailedNotificationRepository {

    public MockAuditFailedNotificationRepository(final Function<AuditFailedNotificationEntity, Long> idGenerator) {
        super(idGenerator);
    }
}
