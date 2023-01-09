package com.synopsys.integration.alert.api.distribution.mock;

import java.util.UUID;
import java.util.function.Function;

import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedEntryRepository extends MockRepositoryContainer<UUID, AuditFailedEntity> implements AuditFailedEntryRepository {
    public MockAuditFailedEntryRepository(final Function<AuditFailedEntity, UUID> idGenerator) {
        super(idGenerator);
    }
}
