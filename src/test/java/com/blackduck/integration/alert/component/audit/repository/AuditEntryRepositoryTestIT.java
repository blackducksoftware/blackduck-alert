/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.repository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.database.audit.AuditEntryEntity;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
class AuditEntryRepositoryTestIT {
    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @AfterEach
    public void cleanup() {
        auditEntryRepository.deleteAll();
    }

    @Test
    @Transactional
    void findFirstByCommonConfigIdOrderByTimeLastSentDescTestIT() {
        UUID commonConfigId = UUID.randomUUID();

        OffsetDateTime leastRecent = OffsetDateTime.ofInstant(Instant.ofEpochMilli(100), ZoneOffset.UTC);
        OffsetDateTime middleDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(200), ZoneOffset.UTC);
        OffsetDateTime mostRecent = OffsetDateTime.ofInstant(Instant.ofEpochMilli(300), ZoneOffset.UTC);

        AuditEntryEntity leastRecentEntity = new AuditEntryEntity(commonConfigId, null, leastRecent, null, null, null);
        AuditEntryEntity middleEntity = new AuditEntryEntity(commonConfigId, null, middleDate, null, null, null);
        AuditEntryEntity mostRecentEntity = new AuditEntryEntity(commonConfigId, null, mostRecent, null, null, null);
        auditEntryRepository.save(leastRecentEntity);
        auditEntryRepository.save(middleEntity);
        auditEntryRepository.save(mostRecentEntity);

        Optional<AuditEntryEntity> foundEntity = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(commonConfigId);
        assertNotNull(foundEntity);
        assertTrue(foundEntity.isPresent());
        assertEquals(mostRecentEntity, foundEntity.get());
    }

    @Test
    @Transactional
    void countByStatusTest() {
        UUID commonConfigId = UUID.randomUUID();

        AuditEntryEntity statusPending = new AuditEntryEntity(commonConfigId, null, null, AuditEntryStatus.PENDING.name(), null, null);
        AuditEntryEntity statusSuccess1 = new AuditEntryEntity(commonConfigId, null, null, AuditEntryStatus.SUCCESS.name(), null, null);
        AuditEntryEntity statusSuccess2 = new AuditEntryEntity(commonConfigId, null, null, AuditEntryStatus.SUCCESS.name(), null, null);
        AuditEntryEntity statusFailure = new AuditEntryEntity(commonConfigId, null, null, AuditEntryStatus.FAILURE.name(), null, null);
        auditEntryRepository.saveAll(List.of(statusPending, statusFailure, statusSuccess1, statusSuccess2));

        assertEquals(1, auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name()));
        assertEquals(2, auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name()));
        assertEquals(1, auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name()));
    }

    @Test
    @Transactional
    void getAverageAuditEntryCompletionTime() {
        UUID commonConfigId = UUID.randomUUID();

        OffsetDateTime startingTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime fiveSecondsOffset = startingTime.plusSeconds(5);
        OffsetDateTime tenSecondsOffset = startingTime.plusSeconds(10);
        OffsetDateTime fifteenSecondsOffset = startingTime.plusSeconds(15);

        AuditEntryEntity entity1 = new AuditEntryEntity(commonConfigId, startingTime, fiveSecondsOffset, AuditEntryStatus.SUCCESS.name(), null, null);
        AuditEntryEntity entity2 = new AuditEntryEntity(commonConfigId, startingTime, tenSecondsOffset, AuditEntryStatus.SUCCESS.name(), null, null);
        AuditEntryEntity entity3 = new AuditEntryEntity(commonConfigId, startingTime, fifteenSecondsOffset, AuditEntryStatus.SUCCESS.name(), null, null);
        auditEntryRepository.saveAll(List.of(entity1, entity2, entity3));

        Optional<String> averageAuditEntryCompletionTime = auditEntryRepository.getAverageAuditEntryCompletionTime();
        assertTrue(averageAuditEntryCompletionTime.isPresent());
        assertEquals("00:00:10", averageAuditEntryCompletionTime.get());
    }

    @Test
    @Transactional
    void getAverageAuditEntryTimeNoEntities() {
        Optional<String> averageAuditEntryCompletionTime = auditEntryRepository.getAverageAuditEntryCompletionTime();
        assertTrue(averageAuditEntryCompletionTime.isEmpty());
    }

    @Test
    @Transactional
    void getAverageAuditEntryTimeUnsetNotifications() {
        UUID commonConfigId = UUID.randomUUID();

        OffsetDateTime startingTime = OffsetDateTime.now(ZoneOffset.UTC);
        AuditEntryEntity entity = new AuditEntryEntity(commonConfigId, startingTime, null, AuditEntryStatus.PENDING.name(), null, null);
        auditEntryRepository.save(entity);

        Optional<String> averageAuditEntryCompletionTime = auditEntryRepository.getAverageAuditEntryCompletionTime();
        assertTrue(averageAuditEntryCompletionTime.isEmpty());
    }
}
