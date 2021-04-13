/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.component.audit.repository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class AuditEntryRepositoryTestIT {
    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Test
    @Transactional
    public void findFirstByCommonConfigIdOrderByTimeLastSentDescTestIT() {
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

}
