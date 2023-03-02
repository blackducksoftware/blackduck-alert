package com.synopsys.integration.alert.database.audit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditFailedEntryRepository extends JpaRepository<AuditFailedEntity, UUID> {

    @Query(value = "SELECT failedAuditEntry "
        + "FROM AuditFailedEntity failedAuditEntry "
        + "LEFT JOIN failedAuditEntry.notification relation ON failedAuditEntry.notificationId = relation.notificationId "
        + "WHERE LOWER(failedAuditEntry.providerName) LIKE %:searchTerm% OR "
        + "LOWER(failedAuditEntry.notificationType) LIKE %:searchTerm% OR "
        + "LOWER(relation.notificationContent) LIKE %:searchTerm% OR "
        + "COALESCE(to_char(failedAuditEntry.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm% OR "
        + "LOWER(failedAuditEntry.jobName) LIKE %:searchTerm% OR "
        + "LOWER(failedAuditEntry.channelName) LIKE %:searchTerm%"
    )
    Page<AuditFailedEntity> findAllWithSearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<AuditFailedEntity> findAllByCreatedAtBefore(OffsetDateTime expirationDate);

    boolean existsByNotificationId(Long notificationId);

    boolean existsById(UUID jobExecutionId);
}
