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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
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
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.audit.AuditEntryActions;
import com.synopsys.integration.alert.web.audit.AuditEntryController;
import com.synopsys.integration.util.ResourceUtil;

import net.minidev.json.JSONArray;

@Transactional
public class AuditEntryHandlerTestIT extends AlertIntegrationTest {
    @Autowired
    public ResponseFactory responseFactory;
    @Autowired
    public Gson gson;
    @Autowired
    private SlackChannelKey slackChannelKey;
    @Autowired
    public AuditEntryRepository auditEntryRepository;
    @Autowired
    public AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private ContentConverter contentConverter;
    @Autowired
    private AuditEntryActions auditEntryActions;
    @Autowired
    private ConfigurationAccessor configurationAccessor;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;

    private ConfigurationModel providerConfigModel = null;

    MockNotificationContent mockNotification = new MockNotificationContent();

    @BeforeEach
    public void init() throws AlertDatabaseConstraintException {
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
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
        mockNotification.setProviderConfigId(providerConfigModel.getConfigurationId());
    }

    @AfterEach
    public void cleanup() throws AlertDatabaseConstraintException {
        configurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());

        auditEntryRepository.flush();
        notificationContentRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();

        auditEntryRepository.deleteAllInBatch();
        notificationContentRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    @Test
    public void getTestIT() throws Exception {
        NotificationEntity savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        notificationContentRepository
            .save(new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{}", 234L, providerConfigModel.getConfigurationId()).createEntity());

        Collection<ConfigurationFieldModel> slackFields = MockConfigurationModelFactory.createSlackDistributionFields();
        ConfigurationJobModel configurationJobModel = configurationAccessor.createJob(Set.of(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey()), slackFields);

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(configurationJobModel.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.eq(ConfigContextEnum.GLOBAL.name()), Mockito.eq(AuditDescriptor.AUDIT_COMPONENT))).thenReturn(true);
        AuditEntryController auditEntryController = new AuditEntryController(auditEntryActions, contentConverter, responseFactory, authorizationManager, new AuditDescriptorKey());

        ResponseEntity<String> response = auditEntryController.get(null, null, null, null, null, true);
        AlertPagedModel<AuditEntryModel> auditEntries = gson.fromJson(response.getBody(), AlertPagedModel.class);
        assertEquals(1, auditEntries.getContent().size());

        ResponseEntity<String> auditEntryResponse = auditEntryController.get(savedNotificationEntity.getId());
        assertNotNull(auditEntryResponse);
        assertEquals(HttpStatus.OK, auditEntryResponse.getStatusCode());

        JSONArray jsonContentArray = JsonPath.read(response.getBody(), "$.content");
        LinkedHashMap auditEntryModelFieldMap = (LinkedHashMap) jsonContentArray.get(0);
        String auditEntryModelString = gson.toJson(auditEntryModelFieldMap);
        AuditEntryModel auditEntry = gson.fromJson(auditEntryModelString, AuditEntryModel.class);

        String auditEntryModelByIdString = JsonPath.read(auditEntryResponse.getBody(), "$.message");
        AuditEntryModel auditEntryById = gson.fromJson(auditEntryModelByIdString, AuditEntryModel.class);
        assertEquals(auditEntryById, auditEntry);

        assertEquals(savedNotificationEntity.getId().toString(), auditEntry.getId());
        assertFalse(auditEntry.getJobs().isEmpty());
        assertEquals(1, auditEntry.getJobs().size());
        FieldAccessor keyToFieldMap = configurationJobModel.getFieldAccessor();
        assertEquals(keyToFieldMap.getString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).get(), auditEntry.getJobs().get(0).getEventType());
        assertEquals(keyToFieldMap.getString(ChannelDistributionUIConfig.KEY_NAME).get(), auditEntry.getJobs().get(0).getName());

        NotificationConfig notification = auditEntry.getNotification();
        String createdAtStringValue = DateUtils.formatDate(savedNotificationEntity.getCreatedAt(), DateUtils.AUDIT_DATE_FORMAT);
        assertEquals(createdAtStringValue, notification.getCreatedAt());
        assertEquals(savedNotificationEntity.getNotificationType(), notification.getNotificationType());
        assertNotNull(notification.getContent());
        response = auditEntryController.get(null, null, null, null, null, false);
        auditEntries = gson.fromJson(response.getBody(), AlertPagedModel.class);
        assertEquals(2, auditEntries.getContent().size());
    }

    @Test
    public void getGetAuditInfoForJobIT() throws Exception {
        Collection<ConfigurationFieldModel> slackFields = MockConfigurationModelFactory.createSlackDistributionFields();
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(slackChannelKey, ConfigContextEnum.DISTRIBUTION, slackFields);
        UUID jobID = UUID.randomUUID();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobID, Set.of(configurationModel));

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(
            new AuditEntryEntity(configurationJobModel.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.eq(ConfigContextEnum.GLOBAL.name()), Mockito.eq(AuditDescriptor.AUDIT_COMPONENT))).thenReturn(true);
        AuditEntryController auditEntryController = new AuditEntryController(auditEntryActions, contentConverter, responseFactory, authorizationManager, new AuditDescriptorKey());

        ResponseEntity<String> jobAuditModelResponse = auditEntryController.getAuditInfoForJob(savedAuditEntryEntity.getCommonConfigId());
        assertNotNull(jobAuditModelResponse);
        assertEquals(HttpStatus.OK, jobAuditModelResponse.getStatusCode());

        assertNotNull(jobAuditModelResponse.getBody());
    }

    @Test
    public void resendNotificationTestIT() throws Exception {
        String content = ResourceUtil.getResourceAsString(getClass(), "/json/policyOverrideNotification.json", StandardCharsets.UTF_8);

        MockNotificationContent mockNotification = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), blackDuckProviderKey.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), "POLICY_OVERRIDE", content, 1L,
            providerConfigModel.getConfigurationId());

        List<ConfigurationFieldModel> slackFieldsList = new ArrayList<>(MockConfigurationModelFactory.createSlackDistributionFields());

        ConfigurationFieldModel providerConfigName = providerConfigModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse(null);
        slackFieldsList.add(providerConfigName);

        ConfigurationJobModel configurationJobModel = configurationAccessor.createJob(Set.of(slackChannelKey.getUniversalKey(), blackDuckProviderKey.getUniversalKey()), slackFieldsList);

        NotificationEntity savedNotificationEntity = notificationContentRepository.save(mockNotification.createEntity());

        AuditEntryEntity savedAuditEntryEntity = auditEntryRepository
                                                     .save(new AuditEntryEntity(configurationJobModel.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(),
                                                         AuditEntryStatus.SUCCESS.toString(),
                                                         null, null));

        auditNotificationRepository.save(new AuditNotificationRelation(savedAuditEntryEntity.getId(), savedNotificationEntity.getId()));

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.eq(ConfigContextEnum.GLOBAL.name()), Mockito.eq(AuditDescriptor.AUDIT_COMPONENT))).thenReturn(true);
        AuditEntryController auditEntryController = new AuditEntryController(auditEntryActions, contentConverter, responseFactory, authorizationManager, new AuditDescriptorKey());

        ResponseEntity<String> invalidIdResponse = auditEntryController.post(-1L, null);
        assertEquals(HttpStatus.GONE, invalidIdResponse.getStatusCode());

        ResponseEntity<String> validResponse = auditEntryController.post(savedNotificationEntity.getId(), null);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());

        ResponseEntity<String> invalidJobResponse = auditEntryController.post(savedNotificationEntity.getId(), UUID.randomUUID());
        assertEquals(HttpStatus.GONE, invalidJobResponse.getStatusCode());

        ResponseEntity<String> invalidReferenceResponse_1 = auditEntryController.post(savedNotificationEntity.getId(), null);
        assertEquals(HttpStatus.OK, invalidReferenceResponse_1.getStatusCode());

        ResponseEntity<String> validJobSpecificResend = auditEntryController.post(savedNotificationEntity.getId(), configurationJobModel.getJobId());
        assertEquals(HttpStatus.OK, validJobSpecificResend.getStatusCode());
    }

}
