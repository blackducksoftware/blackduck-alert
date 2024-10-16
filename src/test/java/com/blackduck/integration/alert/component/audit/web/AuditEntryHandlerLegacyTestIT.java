/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.web;

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

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.api.processor.JobNotificationProcessor;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryModel;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.NotificationConfig;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.component.audit.AuditDescriptor;
import com.blackduck.integration.alert.component.audit.AuditDescriptorKey;
import com.blackduck.integration.alert.database.audit.AuditEntryEntity;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;
import com.blackduck.integration.alert.database.audit.AuditNotificationRepository;
import com.blackduck.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.blackduck.integration.alert.database.configuration.repository.FieldValueRepository;
import com.blackduck.integration.alert.database.notification.NotificationContentRepository;
import com.blackduck.integration.alert.database.notification.NotificationEntity;
import com.blackduck.integration.alert.mock.entity.MockNotificationContent;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.util.ResourceUtil;

/**
 * @deprecated Replaced by AuditEntryController. To be removed in 8.0.0.
 */
@Deprecated(forRemoval = true)
@Transactional
@AlertIntegrationTest
class AuditEntryHandlerLegacyTestIT {
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
    private NotificationMappingProcessor notificationMappingProcessor;
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

    private AuditEntryActionsLegacy createAuditActions(AuthorizationManager authorizationManager) {
        return new AuditEntryActionsLegacy(
            authorizationManager,
            auditDescriptorKey,
            auditAccessor,
            notificationAccessor,
            jobAccessor,
            notificationMappingProcessor,
            jobNotificationProcessor
        );
    }

    @Test
    void getTestIT() {
        NotificationEntity savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        notificationContentRepository
            .save(new MockNotificationContent(
                DateUtils.createCurrentDateTimestamp(),
                "provider",
                DateUtils.createCurrentDateTimestamp(),
                "notificationType",
                "{}",
                234L,
                providerConfigModel.getConfigurationId()
            ).createEntity());

        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel jobModel = jobAccessor.createJob(jobRequestModel);

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(
                jobModel.getJobId(),
                DateUtils.createCurrentDateTimestamp(),
                DateUtils.createCurrentDateTimestamp(),
                AuditEntryStatus.SUCCESS.toString(),
                null,
                null
            ));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);

        AuditEntryActionsLegacy auditEntryActionsLegacy = createAuditActions(authorizationManager);
        AuditEntryPageModel auditEntries = auditEntryActionsLegacy.get(null, null, null, null, null, true).getContent().orElse(null);
        assertEquals(1, auditEntries.getContent().size());

        AuditEntryModel auditEntryResponse = auditEntryActionsLegacy.get(savedNotificationEntity.getId()).getContent().orElse(null);
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

        auditEntries = auditEntryActionsLegacy.get(null, null, null, null, null, false).getContent().orElse(null);
        assertEquals(2, auditEntries.getContent().size());
    }

    @Test
    void getGetAuditInfoForJobIT() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel();
        DistributionJobModel job = jobAccessor.createJob(jobRequestModel);

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(job.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, auditDescriptorKey)).thenReturn(true);
        AuditEntryActionsLegacy auditEntryController = createAuditActions(authorizationManager);

        AuditJobStatusModel jobStatusModel = auditEntryController.getAuditInfoForJob(savedAuditEntryEntity.getCommonConfigId()).getContent().orElse(null);
        assertNotNull(jobStatusModel);
    }

    @Test
    void resendNotificationTestIT() throws Exception {
        String content = ResourceUtil.getResourceAsString(getClass(), "/json/policyOverrideNotification.json", StandardCharsets.UTF_8);

        MockNotificationContent mockNotification = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(),
            blackDuckProviderKey.getUniversalKey(),
            DateUtils.createCurrentDateTimestamp(),
            "POLICY_OVERRIDE",
            content,
            1L,
            providerConfigModel.getConfigurationId()
        );

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
        Mockito.when(authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL.name(), AuditDescriptor.AUDIT_COMPONENT)).thenReturn(true);
        AuditEntryActionsLegacy auditEntryActionsLegacy = createAuditActions(authorizationManager);

        try {
            auditEntryActionsLegacy.resendNotification(savedNotificationEntity.getId(), null);
            auditEntryActionsLegacy.resendNotification(savedNotificationEntity.getId(), null);
            auditEntryActionsLegacy.resendNotification(savedNotificationEntity.getId(), jobModel.getJobId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail("Expected the Audit POST request(s) not to throw an exception");
        }

        assertResponseStatusException(HttpStatus.GONE, () -> auditEntryActionsLegacy.resendNotification(-1L, null));
        assertResponseStatusException(HttpStatus.GONE, () -> auditEntryActionsLegacy.resendNotification(savedNotificationEntity.getId(), UUID.randomUUID()));
    }

    private void assertResponseStatusException(HttpStatus expectedStatus, Supplier<?> auditRequest) {
        try {
            auditRequest.get();
        } catch (ResponseStatusException e) {
            assertEquals(expectedStatus, HttpStatus.resolve(e.getStatusCode().value()));
        }
    }

    private DistributionJobRequestModel createJobRequestModel() {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "test_webhook", null);
        return new DistributionJobRequestModel(
            true,
            "Test Slack Job",
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
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
