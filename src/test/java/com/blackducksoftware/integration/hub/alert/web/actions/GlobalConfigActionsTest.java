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
package com.blackducksoftware.integration.hub.alert.web.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.validator.HubServerConfigValidator;
import com.blackducksoftware.integration.validator.ValidationResults;

public class GlobalConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        Mockito.when(mockedGlobalRepository.exists(Mockito.anyLong())).thenReturn(true);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(mockedGlobalRepository.exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws Exception {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        Mockito.when(mockedGlobalRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createGlobalConfigEntity());
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createGlobalConfigEntity()));
        final GlobalProperties globalProperties = mockUtils.createTestGlobalProperties(mockedGlobalRepository);
        final GlobalConfigRestModel maskedRestModel = mockUtils.createGlobalConfigMaskedRestModel();

        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);
        List<GlobalConfigRestModel> globalConfigsById = configActions.getConfig(1L);
        List<GlobalConfigRestModel> allGlobalConfigs = configActions.getConfig(null);

        assertTrue(globalConfigsById.size() == 1);
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalConfigRestModel globalConfigById = globalConfigsById.get(0);
        final GlobalConfigRestModel globalConfig = allGlobalConfigs.get(0);

        System.out.println(maskedRestModel.toString());
        System.out.println(globalConfigById.toString());

        assertEquals(maskedRestModel, globalConfigById);
        assertEquals(maskedRestModel, globalConfig);

        Mockito.when(mockedGlobalRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(null);

        globalConfigsById = configActions.getConfig(1L);
        allGlobalConfigs = configActions.getConfig(null);

        assertNotNull(globalConfigsById);
        assertNotNull(allGlobalConfigs);
        assertTrue(globalConfigsById.isEmpty());
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalConfigRestModel environmentGlobalConfig = allGlobalConfigs.get(0);
        assertNull(environmentGlobalConfig.getAccumulatorCron());
        assertNull(environmentGlobalConfig.getDailyDigestCron());
        assertEquals("false", environmentGlobalConfig.getHubAlwaysTrustCertificate());
        assertNull(environmentGlobalConfig.getHubPassword());
        assertEquals("HubProxyHost", environmentGlobalConfig.getHubProxyHost());
        assertNull(environmentGlobalConfig.getHubProxyPassword());
        assertEquals("22", environmentGlobalConfig.getHubProxyPort());
        assertEquals("HubProxyUsername", environmentGlobalConfig.getHubProxyUsername());
        assertNull(environmentGlobalConfig.getHubTimeout());
        assertEquals("HubUrl", environmentGlobalConfig.getHubUrl());
        assertNull(environmentGlobalConfig.getHubUsername());
        assertNull(environmentGlobalConfig.getId());

    }

    @Test
    public void testDeleteConfig() {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);
        configActions.deleteConfig(1L);
        verify(mockedGlobalRepository, times(1)).delete(Mockito.anyLong());

        Mockito.reset(mockedGlobalRepository);
        configActions.deleteConfig("1");
        verify(mockedGlobalRepository, times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;
        Mockito.reset(mockedGlobalRepository);
        configActions.deleteConfig(idLong);
        verify(mockedGlobalRepository, times(0)).delete(Mockito.anyLong());

        Mockito.reset(mockedGlobalRepository);
        configActions.deleteConfig(idString);
        verify(mockedGlobalRepository, times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final GlobalConfigEntity expectedGlobalConfigEntity = mockUtils.createGlobalConfigEntity();
        Mockito.when(mockedGlobalRepository.save(Mockito.any(GlobalConfigEntity.class))).thenReturn(expectedGlobalConfigEntity);
        GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);

        GlobalConfigEntity emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalConfigRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedGlobalConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(mockedGlobalRepository.save(Mockito.any(GlobalConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalConfigRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, transformer);

        emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalConfigRestModel());
        assertNull(emailConfigEntity);
    }

    @Test
    public void testValidateConfig() throws Exception {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);

        String response = configActions.validateConfig(mockUtils.createGlobalConfigRestModel());
        assertEquals("Valid", response);

        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "NotInteger", "HubUsername", "HubPassword", "HubProxyHost", "HubProxyPort", "HubProxyUsername", "HubProxyPassword", "NotBoolean", "AccumulatorCron",
                "DailyDigestCron", "PurgeDataCron");

        final Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("hubTimeout", "Not an Integer.");
        fieldErrors.put("hubAlwaysTrustCertificate", "Not an Boolean.");
        fieldErrors.put("accumulatorCron", "Cron expression must consist of 6 fields (found 1 in \"AccumulatorCron\")");
        fieldErrors.put("dailyDigestCron", "Cron expression must consist of 6 fields (found 1 in \"DailyDigestCron\")");
        fieldErrors.put("purgeDataCron", "Cron expression must consist of 6 fields (found 1 in \"PurgeDataCron\")");
        try {
            response = configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            for (final Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                assertTrue(fieldErrors.containsKey(entry.getKey()));
                final String expectedValue = fieldErrors.get(entry.getKey());
                assertEquals(expectedValue, entry.getValue());
            }
        }

        response = configActions.validateConfig(new GlobalConfigRestModel());
        assertEquals("Valid", response);
    }

    @Test
    public void testTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);

        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalConfigRestModel());
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalConfigRestModel fullRestModel = mockUtils.createGlobalConfigRestModel();
        configActions.testConfig(fullRestModel);
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalConfigRestModel partialRestModel = mockUtils.createGlobalConfigMaskedRestModel();

        Mockito.doAnswer(new Answer<GlobalConfigEntity>() {
            @Override
            public GlobalConfigEntity answer(final InvocationOnMock invocation) throws Throwable {
                return mockUtils.createGlobalConfigEntity();
            }
        }).when(mockedGlobalRepository).findOne(Mockito.anyLong());

        configActions.testConfig(partialRestModel);
        // TODO verify that the correct model gets passed into channelTestConfig()
        verify(mockedRestConnection, times(1)).connect();
    }

    @Test
    public void testChannelTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, globalProperties, null, null, null, objectTransformer);
        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalConfigRestModel());
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", "", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *",
                "0 0 12 1/2 * *");
        configActions.channelTestConfig(restModel);
        verify(mockedRestConnection, times(1)).connect();
    }

    @Test
    public void testValidateHubConfiguration() throws Exception {
        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, null, null, null, null);

        final String url = "https://www.google.com/";
        final String user = "User";
        final String password = "Password";
        HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setHubUrl(url);
        serverConfigBuilder.setUsername(user);
        serverConfigBuilder.setUsername(password);

        try {
            configActions.validateHubConfiguration(serverConfigBuilder);
            fail();
        } catch (final AlertFieldException e) {
            assertNotNull(e);
            assertEquals("There were issues with the configuration.", e.getMessage());
            assertTrue(!e.getFieldErrors().isEmpty());
        }

        final HubServerConfigValidator validator = Mockito.mock(HubServerConfigValidator.class);
        serverConfigBuilder = Mockito.spy(serverConfigBuilder);
        Mockito.when(serverConfigBuilder.createValidator()).thenReturn(validator);
        Mockito.when(validator.assertValid()).thenReturn(new ValidationResults());
        try {
            configActions.validateHubConfiguration(serverConfigBuilder);
        } catch (final AlertFieldException e) {
            fail();
        }
    }

    @Test
    public void testCreateRestConnection() throws Exception {
        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, null, null, null, null);

        final String url = "https://www.google.com/";
        final String user = "User";
        final String password = "Password";
        HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setHubUrl(url);
        serverConfigBuilder.setUsername(user);
        serverConfigBuilder.setUsername(password);

        // we create this spy to skip the server validation that happens in the build method
        serverConfigBuilder = Mockito.spy(serverConfigBuilder);
        Mockito.doAnswer(new Answer<HubServerConfig>() {
            @Override
            public HubServerConfig answer(final InvocationOnMock invocation) throws Throwable {
                final Credentials hubCredentials = new Credentials(user, password);
                final HubServerConfig hubServerConfig = new HubServerConfig(new URL(url), 0, hubCredentials, new ProxyInfo(null, 0, null, null), false);
                return hubServerConfig;
            }
        }).when(serverConfigBuilder).build();

        final RestConnection restConnection = configActions.createRestConnection(serverConfigBuilder);
        assertNotNull(restConnection);
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final AccumulatorConfig mockedAccumulatorConfig = Mockito.mock(AccumulatorConfig.class);
        final DailyDigestBatchConfig mockedDailyDigestBatchConfig = Mockito.mock(DailyDigestBatchConfig.class);
        final PurgeConfig mockedPurgeConfig = Mockito.mock(PurgeConfig.class);

        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, mockedAccumulatorConfig, mockedDailyDigestBatchConfig, mockedPurgeConfig, null);
        configActions.configurationChangeTriggers(null);
        verify(mockedAccumulatorConfig, times(0)).scheduleJobExecution(Mockito.any());
        verify(mockedDailyDigestBatchConfig, times(0)).scheduleJobExecution(Mockito.any());
        Mockito.reset(mockedAccumulatorConfig);
        Mockito.reset(mockedDailyDigestBatchConfig);

        final GlobalConfigRestModel restModel = mockUtils.createGlobalConfigRestModel();
        configActions.configurationChangeTriggers(restModel);
        verify(mockedAccumulatorConfig, times(1)).scheduleJobExecution(Mockito.any());
        verify(mockedDailyDigestBatchConfig, times(1)).scheduleJobExecution(Mockito.any());
    }

    @Test
    public void testIsBoolean() {
        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, null, null, null, null);
        assertFalse(configActions.isBoolean(null));
        assertFalse(configActions.isBoolean(""));
        assertFalse(configActions.isBoolean("string"));
        assertFalse(configActions.isBoolean(" cat"));
        assertTrue(configActions.isBoolean("true"));
        assertTrue(configActions.isBoolean("false"));
        assertTrue(configActions.isBoolean("TRUE"));
        assertTrue(configActions.isBoolean("FALSE"));
        assertTrue(configActions.isBoolean("  TruE"));
        assertTrue(configActions.isBoolean("FaLSE  "));
    }

}
