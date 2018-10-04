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
package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Date;

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
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.web.audit.AuditEntryConfig;
import com.synopsys.integration.alert.web.audit.AuditEntryHandler;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.alert.web.model.NotificationConfig;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

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
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private HipChatDistributionRepository hipChatDistributionRepository;

    @Before
    public void cleanup() {
        auditEntryRepository.deleteAll();
        notificationContentRepository.deleteAll();
        commonDistributionRepository.deleteAll();
        hipChatDistributionRepository.deleteAll();
    }

    @Test
    public void getTestIT() {
        final MockNotificationContent mockNotification = new MockNotificationContent();
        final NotificationContent savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
        final CommonDistributionConfigEntity commonDistributionConfigEntity = mockDistributionConfig.createEntity();

        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = hipChatDistributionRepository.save(mockHipChatEntity.createEntity());
        commonDistributionConfigEntity.setDistributionConfigId(hipChatDistributionConfigEntity.getId());

        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(commonDistributionConfigEntity);
        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
                                                           .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        final AlertPagedModel<AuditEntryConfig> auditEntries = auditEntryHandler.get(null, null, null, null, null);
        assertEquals(1, auditEntries.getTotalPages());

        final AuditEntryConfig auditEntry = auditEntryHandler.get(savedAuditEntryEntity.getId());
        assertNotNull(auditEntry);
        assertEquals(auditEntry, auditEntries.getContent().get(0));

        assertEquals(savedAuditEntryEntity.getId().toString(), auditEntry.getId());
        assertEquals(savedConfigEntity.getDistributionType(), auditEntry.getEventType());
        assertEquals(savedConfigEntity.getName(), auditEntry.getName());

        final NotificationConfig notification = auditEntry.getNotification();
        assertEquals(savedNotificationEntity.getCreatedAt().toString(), notification.getCreatedAt());
        assertEquals(savedNotificationEntity.getNotificationType(), notification.getNotificationType());
        assertNotNull(notification.getContent());
    }

    @Test
    public void resendNotificationTestIt() {
        final MockNotificationContent mockNotification = new MockNotificationContent();

        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
        final CommonDistributionConfigEntity commonDistributionConfigEntity = mockDistributionConfig.createEntity();

        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = hipChatDistributionRepository.save(mockHipChatEntity.createEntity());
        commonDistributionConfigEntity.setDistributionConfigId(hipChatDistributionConfigEntity.getId());

        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(commonDistributionConfigEntity);

        final NotificationContent savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());
        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
                                                           .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null));

        final AuditEntryEntity badAuditEntryEntity_1 = auditEntryRepository.save(new AuditEntryEntity(-1L, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, "Failed: stuff happened", ""));
        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));
        final AuditEntryEntity badAuditEntryEntity_2 = auditEntryRepository
                                                           .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE,
                                                               "Failed: stuff happened",
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
