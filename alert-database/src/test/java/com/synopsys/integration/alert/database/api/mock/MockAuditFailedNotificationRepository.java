package com.synopsys.integration.alert.database.api.mock;

import java.util.function.Function;

import com.synopsys.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedNotificationRepository extends MockRepositoryContainer<Long, AuditFailedNotificationEntity> implements AuditFailedNotificationRepository {

    public MockAuditFailedNotificationRepository(final Function<AuditFailedNotificationEntity, Long> idGenerator) {
        super(idGenerator);
    }
}
