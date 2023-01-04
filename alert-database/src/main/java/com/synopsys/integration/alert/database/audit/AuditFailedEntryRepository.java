package com.synopsys.integration.alert.database.audit;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditFailedEntryRepository extends JpaRepository<AuditFailedEntity, UUID> {
}
