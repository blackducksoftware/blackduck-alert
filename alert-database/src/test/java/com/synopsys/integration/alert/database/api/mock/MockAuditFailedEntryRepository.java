package com.synopsys.integration.alert.database.api.mock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Override
    public List<AuditFailedEntity> findAllByCreatedAtBefore(OffsetDateTime expirationDate) {
        Predicate<AuditFailedEntity> dateAfterExpiration = entry -> entry.getCreatedAt().isBefore(expirationDate);
        return getDataMap().values().stream()
            .filter(dateAfterExpiration)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNotificationId(Long notificationId) {
        Predicate<AuditFailedEntity> entryContainsNotification = entry -> entry.getNotificationId().equals(notificationId);
        return getDataMap().values().stream()
            .anyMatch(entryContainsNotification);
    }
}
