package com.synopsys.integration.alert.channel.jira.server.database.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class JiraServerGlobalConfigAccessorTest {
    public static final String TEST_URL = "url";
    public static final String TEST_USERNAME = "username";
    public static final String TEST_PASSWORD = "password";
    private final Gson gson = new Gson();
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
        JiraServerConfigurationEntity entity = createEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        JiraServerGlobalConfigModel configModel = jiraServerGlobalConfigAccessor.getConfiguration(id).orElseThrow(() -> new AlertConfigurationException("Cannot find expected configuration"));
        assertEquals(id.toString(), configModel.getId());
        assertEquals(TEST_URL, configModel.getUrl());
        assertEquals(TEST_USERNAME, configModel.getUserName());
        assertTrue(configModel.getPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, configModel.getPassword().orElse(null));
        assertTrue(configModel.getDisablePluginCheck().orElse(Boolean.FALSE));
    }

    @Test
    void getByConfigurationIdNotFoundTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Optional<JiraServerGlobalConfigModel> configModel = jiraServerGlobalConfigAccessor.getConfiguration(UUID.randomUUID());
        assertTrue(configModel.isEmpty());
    }

    @Test
    void getByConfigurationNameTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createEntity(id);
        Mockito.when(jiraServerConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(entity));
        JiraServerGlobalConfigModel configModel = jiraServerGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).orElseThrow(() -> new AlertConfigurationException("Cannot find expected configuration"));
        assertEquals(id.toString(), configModel.getId());
        assertEquals(TEST_URL, configModel.getUrl());
        assertEquals(TEST_USERNAME, configModel.getUserName());
        assertTrue(configModel.getPasswordSet().orElse(Boolean.FALSE));
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
        JiraServerConfigurationEntity entity = createEntity(id);
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of(entity));
        Mockito.when(jiraServerConfigurationRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(0, 10);
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(1, pagedModel.getModels().size());
    }

    @Test
    void getPageEmptyTest() {
        UUID id = UUID.randomUUID();
        Page<JiraServerConfigurationEntity> jiraConfigurations = new PageImpl<>(List.of());
        Mockito.when(jiraServerConfigurationRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(jiraConfigurations);
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = jiraServerGlobalConfigAccessor.getConfigurationPage(0, 10);
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertEquals(0, pagedModel.getModels().size());
    }

    @Test
    void createConfigurationTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            true);

        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(entity);

        JiraServerGlobalConfigModel createdModel = jiraServerGlobalConfigAccessor.createConfiguration(model);
        assertEquals(entity.getConfigurationId().toString(), createdModel.getId());
        assertEquals(entity.getUrl(), createdModel.getUrl());
        assertEquals(entity.getUsername(), createdModel.getUserName());
        assertTrue(createdModel.getPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, createdModel.getPassword().orElse(null));
        assertEquals(entity.getDisablePluginCheck(), createdModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void createConfigurationModelNullTest() {
        UUID id = UUID.randomUUID();
        JiraServerConfigurationEntity entity = createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            true);

        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(null);

        JiraServerGlobalConfigModel createdModel = jiraServerGlobalConfigAccessor.createConfiguration(model);
        assertNull(createdModel.getId());
        assertNull(createdModel.getUrl());
        assertNull(createdModel.getUserName());
        assertTrue(createdModel.getPasswordSet().isEmpty());
        assertTrue(createdModel.getPassword().isEmpty());
        assertTrue(createdModel.getDisablePluginCheck().isEmpty());
    }

    @Test
    void updateConfigurationTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(entity.getConfigurationId(), updatedName, entity.getCreatedAt(), entity.getLastUpdated(), newUrl, entity.getUsername(), entity.getPassword(),
            entity.getDisablePluginCheck());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            true);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertEquals(updatedEntity.getUsername(), updatedModel.getUserName());
        assertTrue(updatedModel.getPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, updatedModel.getPassword().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateConfigurationPasswordSavedTest() throws AlertConfigurationException {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
        JiraServerConfigurationEntity updatedEntity = new JiraServerConfigurationEntity(entity.getConfigurationId(), updatedName, entity.getCreatedAt(), entity.getLastUpdated(), newUrl, entity.getUsername(), entity.getPassword(),
            entity.getDisablePluginCheck());
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_USERNAME,
            null,
            true,
            true);
        Mockito.when(jiraServerConfigurationRepository.findById(id)).thenReturn(Optional.of(entity));
        Mockito.when(jiraServerConfigurationRepository.save(Mockito.any())).thenReturn(updatedEntity);

        JiraServerGlobalConfigModel updatedModel = jiraServerGlobalConfigAccessor.updateConfiguration(id, model);
        assertEquals(updatedEntity.getConfigurationId().toString(), updatedModel.getId());
        assertEquals(updatedEntity.getUrl(), updatedModel.getUrl());
        assertEquals(updatedEntity.getUsername(), updatedModel.getUserName());
        assertTrue(updatedModel.getPasswordSet().orElse(Boolean.FALSE));
        assertEquals(TEST_PASSWORD, updatedModel.getPassword().orElse(null));
        assertEquals(updatedEntity.getDisablePluginCheck(), updatedModel.getDisablePluginCheck().orElse(null));
    }

    @Test
    void updateConfigurationNotFoundTest() {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newUrl = "https://updated.example.com";
        JiraServerConfigurationEntity entity = createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());

        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.formatDate(entity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            TEST_URL,
            TEST_USERNAME,
            TEST_PASSWORD,
            false,
            true);
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

    private JiraServerConfigurationEntity createEntity(UUID id) {
        return createEntity(id, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private JiraServerConfigurationEntity createEntity(UUID id, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        return new JiraServerConfigurationEntity(id,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            lastUpdated,
            TEST_URL,
            TEST_USERNAME,
            encryptionUtility.encrypt(TEST_PASSWORD),
            true);
    }
}
