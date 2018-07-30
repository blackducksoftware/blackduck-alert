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
package com.blackducksoftware.integration.alert.audit.controller;

import static org.junit.Assert.*;

import java.sql.Date;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackducksoftware.integration.alert.database.DatabaseDataSource;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.NotificationEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.alert.mock.entity.MockNotificationEntity;
import com.blackducksoftware.integration.alert.web.audit.AuditEntryHandler;
import com.blackducksoftware.integration.alert.web.audit.AuditEntryConfig;
import com.blackducksoftware.integration.alert.web.model.AlertPagedModel;
import com.blackducksoftware.integration.alert.web.model.ComponentConfig;
import com.blackducksoftware.integration.alert.web.model.NotificationConfig;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class AuditEntryHandlerTestIT {

    @Autowired
    public AuditEntryRepository auditEntryRepository;
    @Autowired
    public AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private AuditEntryHandler auditEntryHandler;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Before
    public void cleanup() {
        auditEntryRepository.deleteAll();
        notificationRepository.deleteAll();
        commonDistributionRepository.deleteAll();
    }

    @Test
    public void getTestIT() {
        final MockNotificationEntity mockNotification = new MockNotificationEntity();
        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
        final NotificationEntity savedNotificationEntity = notificationRepository.save(mockNotification.createEntity());
        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(mockDistributionConfig.createEntity());
        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
                .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        final AlertPagedModel<AuditEntryConfig> auditEntries = auditEntryHandler.get(null, null);
        assertEquals(1, auditEntries.getTotalPages());

        final AuditEntryConfig auditEntry = auditEntryHandler.get(savedAuditEntryEntity.getId());
        assertNotNull(auditEntry);
        assertEquals(auditEntry, auditEntries.getContent().get(0));

        assertEquals(savedAuditEntryEntity.getId().toString(), auditEntry.getId());
        assertEquals(savedConfigEntity.getDistributionType(), auditEntry.getEventType());
        assertEquals(savedConfigEntity.getName(), auditEntry.getName());

        final NotificationConfig notification = auditEntry.getNotification();
        assertEquals(savedNotificationEntity.getEventKey(), notification.getEventKey());
        assertEquals(savedNotificationEntity.getCreatedAt().toString(), notification.getCreatedAt());
        assertEquals(savedNotificationEntity.getNotificationType().name(), notification.getNotificationTypes().iterator().next());
        assertEquals(savedNotificationEntity.getProjectName(), notification.getProjectName());
        assertEquals(savedNotificationEntity.getProjectVersion(), notification.getProjectVersion());
        assertEquals(savedNotificationEntity.getProjectUrl(), notification.getProjectUrl());
        assertEquals(savedNotificationEntity.getProjectVersionUrl(), notification.getProjectVersionUrl());
        assertNotNull(notification.getComponents());
        assertTrue(!notification.getComponents().isEmpty());
        final ComponentConfig component = notification.getComponents().iterator().next();
        assertEquals(savedNotificationEntity.getComponentName(), component.getComponentName());
        assertEquals(savedNotificationEntity.getComponentVersion(), component.getComponentVersion());
        assertEquals(savedNotificationEntity.getPolicyRuleName(), component.getPolicyRuleName());
        assertEquals(savedNotificationEntity.getPolicyRuleUser(), component.getPolicyRuleUser());
    }

    @Test
    public void resendNotificationTestIt() {
        final MockNotificationEntity mockNotification = new MockNotificationEntity();
        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
        final NotificationEntity savedNotificationEntity = notificationRepository.save(mockNotification.createEntity());
        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(mockDistributionConfig.createEntity());
        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
                .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null));

        final AuditEntryEntity badAuditEntryEntity_1 = auditEntryRepository.save(new AuditEntryEntity(-1L, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, "Failed: stuff happened", ""));
        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));
        final AuditEntryEntity badAuditEntryEntity_2 = auditEntryRepository
                .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, "Failed: stuff happened",
                        ""));
        final AuditEntryEntity badAuditEntryEntityBoth = auditEntryRepository
                .save(new AuditEntryEntity(-1L, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, "Failed: stuff happened", ""));

        final ResponseEntity<String> invalidIdResponse = auditEntryHandler.resendNotification(-1L);
        assertEquals(HttpStatus.BAD_REQUEST, invalidIdResponse.getStatusCode());

        final ResponseEntity<String> invalidReferenceResponse_1 = auditEntryHandler.resendNotification(badAuditEntryEntity_1.getId());
        assertEquals(HttpStatus.GONE, invalidReferenceResponse_1.getStatusCode());

        final ResponseEntity<String> invalidReferenceResponse_2 = auditEntryHandler.resendNotification(badAuditEntryEntity_2.getId());
        assertEquals(HttpStatus.GONE, invalidReferenceResponse_2.getStatusCode());

        final ResponseEntity<String> invalidReferenceResponseBoth = auditEntryHandler.resendNotification(badAuditEntryEntityBoth.getId());
        assertEquals(HttpStatus.GONE, invalidReferenceResponseBoth.getStatusCode());

        final ResponseEntity<String> validResponse = auditEntryHandler.resendNotification(savedAuditEntryEntity.getId());
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
    }

}
