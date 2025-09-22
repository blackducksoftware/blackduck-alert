/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.database.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class JiraServerGlobalConfigAccessorTest {
    static final String TEST_URL = "url";
    static final String TEST_USERNAME = "username";
    static final String TEST_PASSWORD = "password";
    static final String TEST_ACCESS_TOKEN = "access_token";
    static final Integer TEST_DEFAULT_TIMEOUT_IN_SECONDS = 300;

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private JiraServerConfigurationRepository jiraServerConfigurationRepository;

    private JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    @BeforeEach
    public void init() {
        jiraServerConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        jiraServerGlobalConfigAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraServerConfigurationRepository);
    }

    @Test
    void configurationCountTest() {
        Mockito.when(jiraServerConfigurationRepository.count()).thenReturn(1L);
        assertEquals(1L, jiraServerGlobalConfigAccessor.getConfigurationCount());
    }

    @Test
    void getByConfigurationIdTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        JiraServerGlobalConfigModel configModel = jiraServerGlobalConfigAccessor.getConfiguration(id)
            .orElseThrow(() -> new AlertConfigurationException("Cannot find expected configuration"));
        assertEquals(id.toString(), configModel.getId());
        assertEquals(TEST_URL, configModel.getUrl());
        assertEquals(TEST_USERNAME, configModel.getUserName().orElse("Username missing"));
        assertTrue(configModel.getIsPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, configModel.getPassword().orElse(null));
        assertTrue(configModel.getDisablePluginCheck().orElse(Boolean.FALSE));
    }

    @Test
    void getByConfigurationIdNotFoundTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Optional<JiraServerGlobalConfigModel> configModel = jiraServerGlobalConfigAccessor.getConfiguration(UUID.randomUUID());
        assertTrue(configModel.isEmpty());
    }

    @Test
    void getByConfigurationNameTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(entity));
        JiraServerGlobalConfigModel configModel = jiraServerGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .orElseThrow(() -> new AlertConfigurationException("Cannot find expected configuration"));
        assertEquals(id.toString(), configModel.getId());
        assertEquals(TEST_URL, configModel.getUrl());
        assertEquals(TEST_USERNAME, configModel.getUserName().orElse("Username missing"));
        assertTrue(configModel.getIsPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, configModel.getPassword().orElse(null));
        assertTrue(configModel.getDisablePluginCheck().orElse(Boolean.FALSE));
    }

    @Test
    void getByConfigurationNameNotFoundTest() {
        Optional<JiraServerGlobalConfigModel> configModel = jiraServerGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(configModel.isEmpty());
    }

    @Test
    void getPageTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of(entity));
        Mockito.when(jiraServerConfigurationRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(0, 10, null, null, null);
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(1, pagedModel.getModels().size());
    }

    @Test
    void getPageWithSearchTermTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of(entity));
        Mockito.when(jiraServerConfigurationRepository.findBySearchTerm(Mockito.eq(AlertRestConstants.DEFAULT_CONFIGURATION_NAME), Mockito.any(PageRequest.class)))
            .thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            null,
            null
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(1, pagedModel.getModels().size());
    }

    @Test
    void getPageSortAscendingTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        JiraServerConfigurationEntity entity2 = new JiraServerConfigurationEntity(
            id,
            "Another Jira Config",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            encryptionUtility.encrypt(TEST_PASSWORD),
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            true
        );
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of(entity2, entity));
        Mockito.when(jiraServerConfigurationRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")))))
            .thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            "",
            "name",
            "asc"
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(2, pagedModel.getModels().size());
        assertEquals("Another Jira Config", pagedModel.getModels().get(0).getName());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(1).getName());
    }

    @Test
    void getPageSortDescendingTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id);
        JiraServerConfigurationEntity entity2 = new JiraServerConfigurationEntity(
            id,
            "Another Jira Config",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            encryptionUtility.encrypt(TEST_PASSWORD),
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            true
        );
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of(entity, entity2));
        Mockito.when(jiraServerConfigurationRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")))))
            .thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            "",
            "name",
            "asc"
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(2, pagedModel.getModels().size());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(0).getName());
        assertEquals("Another Jira Config", pagedModel.getModels().get(1).getName());
    }

    @Test
    void getPageEmptyTest() {
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of());
        Mockito.when(jiraServerConfigurationRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(0, 10, null, null, null);
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertEquals(0, pagedModel.getModels().size());
    }

    @Test
    void createBasicConfigurationTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            null,
            false,
            true
        );

        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(entity);

        JiraServerGlobalConfigModel createdModel = jiraServerGlobalConfigAccessor.createConfiguration(model);
        assertEquals(entity.getConfigurationId().toString(), createdModel.getId());
        assertEquals(entity.getUrl(), createdModel.getUrl());
        assertEquals(entity.getUsername(), createdModel.getUserName().orElse("Username missing"));
        assertTrue(createdModel.getIsPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, createdModel.getPassword().orElse(null));
        assertEquals(entity.getDisablePluginCheck(), createdModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void createPersonalAccessTokenConfigurationTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createPersonalAccessTokenAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            null,
            null,
            false,
            TEST_ACCESS_TOKEN,
            false,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(entity);

        JiraServerGlobalConfigModel createdModel = jiraServerGlobalConfigAccessor.createConfiguration(model);
        assertEquals(entity.getConfigurationId().toString(), createdModel.getId());
        assertEquals(entity.getUrl(), createdModel.getUrl());
        assertTrue(createdModel.getUserName().isEmpty());
        assertTrue(createdModel.getPassword().isEmpty());
        assertTrue(createdModel.getIsAccessTokenSet().orElse(Boolean.FALSE));
        assertEquals(TEST_ACCESS_TOKEN, createdModel.getAccessToken().orElse("No access token saved"));

        assertEquals(entity.getDisablePluginCheck(), createdModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateBasicAuthConfigurationTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(
            entity.getConfigurationId(),
            updatedName,
            entity.getCreatedAt(),
            entity.getLastUpdated(),
            newUrl,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            entity.getUsername(),
            entity.getPassword(),
            null,
            entity.getDisablePluginCheck()
        );
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            null,
            false,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertEquals(updatedEntity.getUsername(), updatedModel.getUserName().orElse("Username missing"));
        assertTrue(updatedModel.getIsPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, updatedModel.getPassword().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updatePersonalAccessTokenAuthConfigurationTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createPersonalAccessTokenAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(
            entity.getConfigurationId(),
            updatedName,
            entity.getCreatedAt(),
            entity.getLastUpdated(),
            newUrl,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            null,
            null,
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            entity.getDisablePluginCheck()
        );
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            null,
            null,
            false,
            TEST_ACCESS_TOKEN,
            false,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertTrue(updatedModel.getUserName().isEmpty());
        assertTrue(updatedModel.getPassword().isEmpty());
        assertTrue(updatedModel.getIsAccessTokenSet().orElse(Boolean.FALSE));
        assertEquals(TEST_ACCESS_TOKEN, updatedModel.getAccessToken().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateConfigurationPasswordSavedTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(
            entity.getConfigurationId(),
            updatedName,
            entity.getCreatedAt(),
            entity.getLastUpdated(),
            newUrl,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            entity.getUsername(),
            entity.getPassword(),
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            entity.getDisablePluginCheck()
        );
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            null,
            true,
            null,
            false,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertEquals(updatedEntity.getUsername(), updatedModel.getUserName().orElse("Username missing"));
        assertTrue(updatedModel.getIsPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, updatedModel.getPassword().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateConfigurationAccessTokenSavedTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createPersonalAccessTokenAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(
            entity.getConfigurationId(),
            updatedName,
            entity.getCreatedAt(),
            entity.getLastUpdated(),
            newUrl,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            entity.getUsername(),
            entity.getPassword(),
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            entity.getDisablePluginCheck()
        );
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            TEST_USERNAME,
            null,
            false,
            null,
            true,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertTrue(updatedModel.getUserName().isEmpty());
        assertTrue(updatedModel.getPassword().isEmpty());
        assertTrue(updatedModel.getIsAccessTokenSet().orElse(Boolean.FALSE));
        assertEquals(TEST_ACCESS_TOKEN, updatedModel.getAccessToken().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateConfigurationNotFoundTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createBasicAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());

        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            null,
            false,
            true
        );
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.empty());

        try {
            jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
            fail("Exception expected because id not found");
        } catch (AlertConfigurationException ex) {
            // expected to get here
        }
    }

    @Test
    void deleteConfigurationTest() {
        UUID id = UUID.randomUUID();
        jiraServerGlobalConfigAccessor.deleteConfiguration(id);
        Mockito.verify(jiraServerConfigurationRepository).deleteById(id);
    }

    @Test
    void deleteConfigurationNullTest() {
        jiraServerGlobalConfigAccessor.deleteConfiguration(null);
        Mockito.verify(jiraServerConfigurationRepository, Mockito.times(0)).deleteById(Mockito.any());
    }

    private JiraServerConfigurationEntity createBasicAuthEntity(UUID id) {
        return createBasicAuthEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private JiraServerConfigurationEntity createBasicAuthEntity(UUID id, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        return new JiraServerConfigurationEntity(
            id,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            lastUpdated,
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USERNAME,
            encryptionUtility.encrypt(TEST_PASSWORD),
            null,
            true
        );
    }

    private JiraServerConfigurationEntity createPersonalAccessTokenAuthEntity(UUID id, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        return new JiraServerConfigurationEntity(
            id,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            lastUpdated,
            TEST_URL,
            TEST_DEFAULT_TIMEOUT_IN_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            null,
            null,
            encryptionUtility.encrypt(TEST_ACCESS_TOKEN),
            true
        );
    }
}
