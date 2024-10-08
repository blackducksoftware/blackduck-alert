package com.synopsys.integration.alert.api.distribution.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryNotificationView;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAuditEntryRepository extends MockRepositoryContainer<Long, AuditEntryEntity> implements AuditEntryRepository {
    private final AuditNotificationRepository auditNotificationRepository;

    public MockAuditEntryRepository(Function<AuditEntryEntity, Long> idGenerator, AuditNotificationRepository auditNotificationRepository) {
        super(idGenerator);
        this.auditNotificationRepository = auditNotificationRepository;
    }

    @Override
    public List<AuditEntryNotificationView> findByJobIdAndNotificationIds(UUID jobId, Collection<Long> notificationIds) {

        List<AuditEntryEntity> auditEntries = getDataMap().values().stream()
            .filter(entry -> entry.getCommonConfigId().equals(jobId))
            .collect(Collectors.toList());
        List<AuditEntryNotificationView> views = new ArrayList<>(auditEntries.size());
        for (AuditEntryEntity entity : auditEntries) {
            List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(entity.getId());
            for (AuditNotificationRelation relation : relations) {
                views.add(convertToView(entity, relation));
            }
        }

        return views;
    }

    private AuditEntryNotificationView convertToView(AuditEntryEntity entity, AuditNotificationRelation relation) {
        return new AuditEntryNotificationView(
            entity.getId(),
            entity.getCommonConfigId(),
            relation.getNotificationId(),
            entity.getTimeCreated(),
            entity.getTimeLastSent(),
            entity.getStatus(),
            entity.getErrorMessage(),
            entity.getErrorStackTrace()
        );
    }

    @Override
    public Optional<AuditEntryEntity> findFirstByCommonConfigIdOrderByTimeLastSentDesc(UUID commonConfigId) {
        return getDataMap().values().stream()
            .sorted(Comparator.comparing(AuditEntryEntity::getTimeLastSent))
            .filter(entry -> entry.getCommonConfigId().equals(commonConfigId))
            .findFirst();
    }

    @Override
    public Optional<AuditEntryEntity> findMatchingAudit(Long notificationId, UUID commonConfigId) {
        List<AuditEntryEntity> auditEntries = getDataMap().values().stream()
            .filter(entry -> entry.getCommonConfigId().equals(commonConfigId))
            .collect(Collectors.toList());
        for (AuditEntryEntity entity : auditEntries) {
            Optional<AuditNotificationRelation> relationFound = auditNotificationRepository.findByAuditEntryId(entity.getId()).stream()
                .filter(relation -> relation.getNotificationId().equals(notificationId))
                .findFirst();
            if (relationFound.isPresent()) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    @Override
    public void bulkDeleteOrphanedEntries() {
        // not implemented
    }

    @Override
    public long countByStatus(String status) {
        return getDataMap().values().stream()
            .filter(entry -> entry.getStatus().equals(status))
            .count();
    }

    @Override
    public Optional<String> getAverageAuditEntryCompletionTime() {
        // not implemented
        return Optional.empty();
    }
}
