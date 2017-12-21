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
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalHubConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.validator.HubServerConfigValidator;
import com.blackducksoftware.integration.validator.ValidationResults;

public class GlobalHubConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        Mockito.when(mockedGlobalRepository.exists(Mockito.anyLong())).thenReturn(true);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
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
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        Mockito.when(mockedGlobalRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createGlobalHubConfigEntity());
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createGlobalHubConfigEntity()));
        final GlobalProperties globalProperties = mockUtils.createTestGlobalProperties(mockedGlobalRepository, null);
        final GlobalHubConfigRestModel maskedRestModel = mockUtils.createGlobalHubConfigMaskedRestModel();

        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
        List<GlobalHubConfigRestModel> globalConfigsById = configActions.getConfig(1L);
        List<GlobalHubConfigRestModel> allGlobalConfigs = configActions.getConfig(null);

        assertTrue(globalConfigsById.size() == 1);
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalHubConfigRestModel globalConfigById = globalConfigsById.get(0);
        final GlobalHubConfigRestModel globalConfig = allGlobalConfigs.get(0);

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

        final GlobalHubConfigRestModel environmentGlobalConfig = allGlobalConfigs.get(0);
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
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
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
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        final GlobalHubConfigEntity expectedGlobalConfigEntity = mockUtils.createGlobalHubConfigEntity();
        Mockito.when(mockedGlobalRepository.save(Mockito.any(GlobalHubConfigEntity.class))).thenReturn(expectedGlobalConfigEntity);
        GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);

        GlobalHubConfigEntity emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalHubConfigRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedGlobalConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(mockedGlobalRepository.save(Mockito.any(GlobalHubConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalHubConfigRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, transformer);

        emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalHubConfigRestModel());
        assertNull(emailConfigEntity);
    }

    @Test
    public void testValidateConfig() throws Exception {
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);

        String response = configActions.validateConfig(mockUtils.createGlobalHubConfigRestModel());
        assertEquals("Valid", response);

        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "NotInteger", "HubUsername", "HubPassword", true, "HubProxyHost", "HubProxyPort", "HubProxyUsername", "HubProxyPassword", true, "NotBoolean");

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

        response = configActions.validateConfig(new GlobalHubConfigRestModel());
        assertEquals("Valid", response);
    }

    @Test
    public void testTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);

        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalHubConfigRestModel());
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel fullRestModel = mockUtils.createGlobalHubConfigRestModel();
        configActions.testConfig(fullRestModel);
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel partialRestModel = mockUtils.createGlobalHubConfigMaskedRestModel();

        Mockito.doAnswer(new Answer<GlobalHubConfigEntity>() {
            @Override
            public GlobalHubConfigEntity answer(final InvocationOnMock invocation) throws Throwable {
                return mockUtils.createGlobalHubConfigEntity();
            }
        }).when(mockedGlobalRepository).findOne(Mockito.anyLong());

        configActions.testConfig(partialRestModel);
        // TODO verify that the correct model gets passed into channelTestConfig()
        verify(mockedRestConnection, times(1)).connect();
    }

    @Test
    public void testChannelTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository, null);
        GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalHubConfigRestModel());
        verify(mockedRestConnection, times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", true, "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", true, "");
        configActions.channelTestConfig(restModel);
        verify(mockedRestConnection, times(1)).connect();
    }

    @Test
    public void testValidateHubConfiguration() throws Exception {
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(null, null, null);

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
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(null, null, null);

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
    public void testIsBoolean() {
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(null, null, null);
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
