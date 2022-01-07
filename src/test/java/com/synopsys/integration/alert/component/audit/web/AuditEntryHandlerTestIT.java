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
package com.synopsys.integration.alert.component.audit.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.audit.AuditDescriptor;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.processor.api.JobNotificationProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.util.ResourceUtil;

@Transactional
@AlertIntegrationTest
public class AuditEntryHandlerTestIT {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuditDescriptorKey auditDescriptorKey;
    @Autowired
    public AuditEntryRepository auditEntryRepository;
    @Autowired
    public AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;
    @Autowired
    private RestApiAuditAccessor auditAccessor;
    @Autowired
    private NotificationProcessor notificationProcessor;
    @Autowired
    private JobNotificationProcessor jobNotificationProcessor;
    @Autowired
    private NotificationAccessor notificationAccessor;

    private ConfigurationModel providerConfigModel = null;

    MockNotificationContent mockNotification = new MockNotificationContent();

    @BeforeEach
    public void init() {
        auditEntryRepository.flush();
        notificationContentRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();

        auditEntryRepository.deleteAllInBatch();
        notificationContentRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();

        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
        mockNotification.setProviderConfigId(providerConfigModel.getConfigurationId());
    }

    @AfterEach
    public void cleanup() {
        configurationModelConfigurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());

        auditEntryRepository.flush();
        notificationContentRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();

        auditEntryRepository.deleteAllInBatch();
        notificationContentRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    private AuditEntryActions createAuditActions(AuthorizationManager authorizationManager) {
        return new AuditEntryActions(authorizationManager, auditDescriptorKey, auditAccessor, notificationAccessor, jobAccessor, notificationProcessor, jobNotificationProcessor);
    }

    @Test
    public void getTestIT() {
        NotificationEntity savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        notificationContentRepository
            .save(new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{}", 234L, providerConfigModel.getConfigurationId()).createEntity());

        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel jobModel = jobAccessor.createJob(jobRequestModel);

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(jobModel.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);

        AuditEntryActions auditEntryActions = createAuditActions(authorizationManager);
        AuditEntryPageModel auditEntries = auditEntryActions.get(null, null, null, null, null, true).getContent().orElse(null);
        assertEquals(1, auditEntries.getContent().size());

        AuditEntryModel auditEntryResponse = auditEntryActions.get(savedNotificationEntity.getId()).getContent().orElse(null);
        assertNotNull(auditEntryResponse);

        AuditEntryModel auditEntry = auditEntries.getContent().get(0);
        assertEquals(savedNotificationEntity.getId().toString(), auditEntry.getId());
        assertFalse(auditEntry.getJobs().isEmpty());
        assertEquals(1, auditEntry.getJobs().size());

        NotificationConfig notification = auditEntry.getNotification();
        String createdAtStringValue = DateUtils.formatDate(savedNotificationEntity.getCreatedAt(), DateUtils.AUDIT_DATE_FORMAT);
        assertEquals(createdAtStringValue, notification.getCreatedAt());
        assertEquals(savedNotificationEntity.getNotificationType(), notification.getNotificationType());
        assertNotNull(notification.getContent());

        auditEntries = auditEntryActions.get(null, null, null, null, null, false).getContent().orElse(null);
        assertEquals(2, auditEntries.getContent().size());
    }

    @Test
    public void getGetAuditInfoForJobIT() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel job = jobAccessor.createJob(jobRequestModel);

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(job.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.eq(ConfigContextEnum.GLOBAL), Mockito.eq(auditDescriptorKey))).thenReturn(true);
        AuditEntryActions auditEntryController = createAuditActions(authorizationManager);

        AuditJobStatusModel jobStatusModel = auditEntryController.getAuditInfoForJob(savedAuditEntryEntity.getCommonConfigId()).getContent().orElse(null);
        assertNotNull(jobStatusModel);
    }

    @Test
    public void resendNotificationTestIT() throws Exception {
        String content = ResourceUtil.getResourceAsString(getClass(), "/json/policyOverrideNotification.json", StandardCharsets.UTF_8);

        MockNotificationContent mockNotification = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), blackDuckProviderKey.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), "POLICY_OVERRIDE", content, 1L,
            providerConfigModel.getConfigurationId());

        ConfigurationFieldModel providerConfigId = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        providerConfigId.setFieldValue(String.valueOf(providerConfigModel.getConfigurationId()));

        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel jobModel = jobAccessor.createJob(jobRequestModel);

        NotificationEntity savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
            .save(new AuditEntryEntity(jobModel.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(),
                AuditEntryStatus.SUCCESS.toString(),
                null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.eq(ConfigContextEnum.GLOBAL.name()), Mockito.eq(AuditDescriptor.AUDIT_COMPONENT))).thenReturn(true);
        AuditEntryActions auditEntryActions = createAuditActions(authorizationManager);

        try {
            auditEntryActions.resendNotification(savedNotificationEntity.getId(), null);
            auditEntryActions.resendNotification(savedNotificationEntity.getId(), null);
            auditEntryActions.resendNotification(savedNotificationEntity.getId(), jobModel.getJobId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail("Expected the Audit POST request(s) not to throw an exception");
        }

        assertResponseStatusException(HttpStatus.GONE, () -> auditEntryActions.resendNotification(-1L, null));
        assertResponseStatusException(HttpStatus.GONE, () -> auditEntryActions.resendNotification(savedNotificationEntity.getId(), UUID.randomUUID()));
    }

    private void assertResponseStatusException(HttpStatus expectedStatus, Supplier<?> auditRequest) {
        try {
            auditRequest.get();
        } catch (ResponseStatusException e) {
            assertEquals(expectedStatus, e.getStatus());
        }
    }

    private DistributionJobRequestModel createJobRequestModel() {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "test_webhook", "#test-channel", null);
        return new DistributionJobRequestModel(
            true,
            "Test Slack Job",
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            providerConfigModel.getConfigurationId(),
            false,
            null,
            null,
            List.of("notificationType"),
            List.of(),
            List.of(),
            List.of(),
            details
        );
    }

}
