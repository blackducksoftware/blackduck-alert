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
package com.synopsys.integration.alert.audit.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;

public class AuditEntryRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Test
    public void findFirstByCommonConfigIdOrderByTimeLastSentDescTestIT() {
        final Long commonConfigId = 50L;

        final Date leastRecent = new Date(100);
        final Date middleDate = new Date(200);
        final Date mostRecent = new Date(300);

        final AuditEntryEntity leastRecentEntity = new AuditEntryEntity(commonConfigId, null, leastRecent, null, null, null);
        final AuditEntryEntity middleEntity = new AuditEntryEntity(commonConfigId, null, middleDate, null, null, null);
        final AuditEntryEntity mostRecentEntity = new AuditEntryEntity(commonConfigId, null, mostRecent, null, null, null);
        auditEntryRepository.save(leastRecentEntity);
        auditEntryRepository.save(middleEntity);
        auditEntryRepository.save(mostRecentEntity);

        final AuditEntryEntity foundEntity = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(commonConfigId);
        assertNotNull(foundEntity);
        assertEquals(mostRecentEntity, foundEntity);
    }

}
