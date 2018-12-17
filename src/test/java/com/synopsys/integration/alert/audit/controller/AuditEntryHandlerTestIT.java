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

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;
import com.synopsys.integration.alert.web.audit.AuditEntryHandler;

public class AuditEntryHandlerTestIT extends AlertIntegrationTest {

    @Autowired
    public AuditEntryRepository auditEntryRepository;
    @Autowired
    public AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private AuditEntryHandler auditEntryHandler;
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private BaseConfigurationAccessor baseConfigurationAccessor;
    @Autowired
    private DistributionNotificationTypeRepository distributionNotificationTypeRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Before
    public void cleanup() {
        auditEntryRepository.deleteAll();
        notificationContentRepository.deleteAll();
        descriptorConfigRepository.deleteAll();
        fieldValueRepository.deleteAll();
    }

    //    @Test
    //    public void getTestIT() throws Exception {
    //        final MockNotificationContent mockNotification = new MockNotificationContent();
    //        final NotificationContent savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());
    //
    //        notificationContentRepository.save(new MockNotificationContent(new Date(System.currentTimeMillis()), "provider", new Date(System.currentTimeMillis()), "notificationType", "{}", 234L).createEntity());
    //
    //        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
    //        final CommonDistributionConfigEntity commonDistributionConfigEntity = mockDistributionConfig.createEntity();
    //
    //        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
    //        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = hipChatDistributionRepository.save(mockHipChatEntity.createEntity());
    //        commonDistributionConfigEntity.setDistributionConfigId(hipChatDistributionConfigEntity.getId());
    //
    //        baseConfigurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION, null);
    //
    //        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(commonDistributionConfigEntity);
    //        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
    //            new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS.toString(), null, null));
    //
    //        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));
    //
    //        AlertPagedModel<AuditEntryModel> auditEntries = auditEntryHandler.get(null, null, null, null, null, true);
    //        assertEquals(1, auditEntries.getContent().size());
    //
    //        final AuditEntryModel auditEntry = auditEntryHandler.get(savedNotificationEntity.getId());
    //        assertNotNull(auditEntry);
    //        assertEquals(auditEntry, auditEntries.getContent().get(0));
    //
    //        assertEquals(savedNotificationEntity.getId().toString(), auditEntry.getId());
    //        assertFalse(auditEntry.getJobs().isEmpty());
    //        assertTrue(1 == auditEntry.getJobs().size());
    //        assertEquals(savedConfigEntity.getDistributionType(), auditEntry.getJobs().get(0).getEventType());
    //        assertEquals(savedConfigEntity.getName(), auditEntry.getJobs().get(0).getName());
    //
    //        final NotificationConfig notification = auditEntry.getNotification();
    //        assertEquals(savedNotificationEntity.getCreatedAt().toString(), notification.getCreatedAt());
    //        assertEquals(savedNotificationEntity.getNotificationType(), notification.getNotificationType());
    //        assertNotNull(notification.getContent());
    //
    //        auditEntries = auditEntryHandler.get(null, null, null, null, null, false);
    //        assertEquals(2, auditEntries.getContent().size());
    //    }
    //
    //    @Test
    //    public void resendNotificationTestIt() throws Exception {
    //        final String content = ResourceUtil.getResourceAsString(this.getClass(), "/json/policyOverrideNotification.json", StandardCharsets.UTF_8);
    //
    //        final MockNotificationContent mockNotification = new MockNotificationContent(new java.util.Date(), BlackDuckProvider.COMPONENT_NAME, new java.util.Date(), "POLICY_OVERRIDE", content, 1L);
    //
    //        final MockCommonDistributionEntity mockDistributionConfig = new MockCommonDistributionEntity();
    //        final CommonDistributionConfigEntity commonDistributionConfigEntity = mockDistributionConfig.createEntity();
    //
    //        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
    //        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = hipChatDistributionRepository.save(mockHipChatEntity.createEntity());
    //        commonDistributionConfigEntity.setDistributionConfigId(hipChatDistributionConfigEntity.getId());
    //
    //        final CommonDistributionConfigEntity savedConfigEntity = commonDistributionRepository.save(commonDistributionConfigEntity);
    //
    //        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(savedConfigEntity.getId(), "POLICY_OVERRIDE"));
    //
    //        final NotificationContent savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());
    //
    //        final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
    //                                                           .save(new AuditEntryEntity(savedConfigEntity.getId(), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS.toString(), null, null));
    //
    //        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));
    //
    //        final ResponseEntity<String> invalidIdResponse = auditEntryHandler.resendNotification(-1L, null);
    //        assertEquals(HttpStatus.GONE, invalidIdResponse.getStatusCode());
    //
    //        final ResponseEntity<String> validResponse = auditEntryHandler.resendNotification(savedNotificationEntity.getId(), null);
    //        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
    //
    //        final ResponseEntity<String> invalidJobResponse = auditEntryHandler.resendNotification(savedNotificationEntity.getId(), -1L);
    //        assertEquals(HttpStatus.GONE, invalidJobResponse.getStatusCode());
    //
    //        final ResponseEntity<String> invalidReferenceResponse_1 = auditEntryHandler.resendNotification(savedNotificationEntity.getId(), null);
    //        assertEquals(HttpStatus.OK, invalidReferenceResponse_1.getStatusCode());
    //
    //        final ResponseEntity<String> validJobSpecificResend = auditEntryHandler.resendNotification(savedNotificationEntity.getId(), savedConfigEntity.getId());
    //        assertEquals(HttpStatus.OK, validJobSpecificResend.getStatusCode());
    //    }

}
