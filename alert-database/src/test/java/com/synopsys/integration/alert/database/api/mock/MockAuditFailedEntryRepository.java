package com.synopsys.integration.alert.database.api.mock;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditFailedEntryRepository extends MockRepositoryContainer<UUID, AuditFailedEntity> implements AuditFailedEntryRepository {
    public MockAuditFailedEntryRepository(final Function<AuditFailedEntity, UUID> idGenerator) {
        super(idGenerator);
    }

    @Override
    public Page<AuditFailedEntity> findAllWithSearchTerm(final String searchTerm, final Pageable pageable) {
        return Page.empty();
    }
}
