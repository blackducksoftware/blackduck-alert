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
package com.blackducksoftware.integration.hub.alert.audit.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class AuditEntryRepositoryTestIT {
    @Autowired
    private AuditEntryRepositoryWrapper auditEntryRepository;

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
