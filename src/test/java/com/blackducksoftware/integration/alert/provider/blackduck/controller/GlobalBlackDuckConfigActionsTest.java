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
package com.blackducksoftware.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.blackducksoftware.integration.alert.provider.hub.descriptor.BlackDuckContentConverter;
import com.blackducksoftware.integration.alert.web.actions.GlobalActionsTest;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfigActions;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigValidator;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.validator.ValidationResults;

public class GlobalBlackDuckConfigActionsTest extends GlobalActionsTest<GlobalBlackDuckConfig, GlobalBlackDuckConfigEntity, GlobalBlackDuckRepository, GlobalBlackDuckConfigActions> {

    @Override
    public GlobalBlackDuckConfigActions getMockedConfigActions() {
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(mockedGlobalRepository);

        final GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(mockedGlobalRepository, hubProperties, new BlackDuckContentConverter(getContentConverter()));
        return configActions;
    }

    @Override
    public Class<GlobalBlackDuckConfigEntity> getGlobalEntityClass() {
        return GlobalBlackDuckConfigEntity.class;
    }

    @Test
    @Override
    public void testGetConfig() throws Exception {
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        Mockito.when(mockedGlobalRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(getGlobalEntityMockUtil().createGlobalEntity()));
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(Arrays.asList(getGlobalEntityMockUtil().createGlobalEntity()));
        final GlobalBlackDuckConfigEntity databaseEntity = getGlobalEntityMockUtil().createGlobalEntity();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository);
        globalProperties.setBlackDuckTrustCertificate(null);
        globalProperties.setBlackDuckUrl(null);
        final BlackDuckContentConverter hubContentConverter = new BlackDuckContentConverter(getContentConverter());
        final GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(mockedGlobalRepository, globalProperties, hubContentConverter);
        final GlobalBlackDuckConfig defaultRestModel = (GlobalBlackDuckConfig) hubContentConverter.populateRestModelFromDatabaseEntity(databaseEntity);
        final GlobalBlackDuckConfig maskedRestModel = configActions.maskRestModel(defaultRestModel);
        List<GlobalBlackDuckConfig> globalConfigsById = configActions.getConfig(1L);
        List<GlobalBlackDuckConfig> allGlobalConfigs = configActions.getConfig(null);

        assertTrue(globalConfigsById.size() == 1);
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalBlackDuckConfig globalConfigById = globalConfigsById.get(0);
        final GlobalBlackDuckConfig globalConfig = allGlobalConfigs.get(0);

        System.out.println(maskedRestModel.toString());
        System.out.println(globalConfigById.toString());

        assertEquals(maskedRestModel, globalConfigById);
        assertEquals(maskedRestModel, globalConfig);

        Mockito.when(mockedGlobalRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(Arrays.asList());

        globalConfigsById = configActions.getConfig(1L);
        allGlobalConfigs = configActions.getConfig(null);

        assertNotNull(globalConfigsById);
        assertNotNull(allGlobalConfigs);
        assertTrue(globalConfigsById.isEmpty());
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalBlackDuckConfig environmentGlobalConfig = allGlobalConfigs.get(0);
        assertEquals(maskedRestModel.getBlackDuckAlwaysTrustCertificate(), environmentGlobalConfig.getBlackDuckAlwaysTrustCertificate());
        assertNull(environmentGlobalConfig.getBlackDuckApiKey());
        assertEquals(maskedRestModel.getBlackDuckProxyHost(), environmentGlobalConfig.getBlackDuckProxyHost());
        assertNull(environmentGlobalConfig.getBlackDuckProxyPassword());
        assertEquals(maskedRestModel.getBlackDuckProxyPort(), environmentGlobalConfig.getBlackDuckProxyPort());
        assertEquals(maskedRestModel.getBlackDuckProxyUsername(), environmentGlobalConfig.getBlackDuckProxyUsername());
        assertNull(environmentGlobalConfig.getBlackDuckTimeout());
        assertEquals(maskedRestModel.getBlackDuckUrl(), environmentGlobalConfig.getBlackDuckUrl());
        assertNull(environmentGlobalConfig.getId());
    }

    @Test
    public void testTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository);
        GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(mockedGlobalRepository, globalProperties, new BlackDuckContentConverter(getContentConverter()));

        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateBlackDuckConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(getGlobalRestModelMockUtil().createGlobalRestModel());
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalBlackDuckConfig fullRestModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.testConfig(fullRestModel);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalBlackDuckConfig restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        final GlobalBlackDuckConfig partialRestModel = configActions.maskRestModel(restModel);

        Mockito.doAnswer(new Answer<Optional<GlobalBlackDuckConfigEntity>>() {
            @Override
            public Optional<GlobalBlackDuckConfigEntity> answer(final InvocationOnMock invocation) throws Throwable {
                return Optional.of(getGlobalEntityMockUtil().createGlobalEntity());
            }
        }).when(mockedGlobalRepository).findById(Mockito.anyLong());

        final String result = configActions.testConfig(partialRestModel);
        assertEquals("Successfully connected to the Hub.", result);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();

    }

    @Test
    public void testChannelTestConfig() throws Exception {
        final MockGlobalBlackDuckRestModel mockUtils = new MockGlobalBlackDuckRestModel();
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalBlackDuckRepository mockedGlobalRepository = Mockito.mock(GlobalBlackDuckRepository.class);
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(mockedGlobalRepository);
        GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(mockedGlobalRepository, globalProperties, new BlackDuckContentConverter(getContentConverter()));
        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateBlackDuckConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalRestModel());
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalBlackDuckConfig restModel = mockUtils.createGlobalRestModel();
        final String result = configActions.channelTestConfig(restModel);
        assertEquals("Successfully connected to the Hub.", result);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
    }

    @Override
    public void testInvalidConfig() {
        final MockGlobalBlackDuckRestModel mockUtil = new MockGlobalBlackDuckRestModel();
        mockUtil.setBlackDuckTimeout("qqq");
        final GlobalBlackDuckConfig restModel = mockUtil.createGlobalRestModel();

        String result = null;
        try {
            result = configActions.validateConfig(restModel);
            fail();
        } catch (final AlertFieldException e) {
            assertTrue(true);
        }

        assertNull(result);

        mockUtil.setBlackDuckApiKey(StringUtils.repeat('a', 300));
        final GlobalBlackDuckConfig restModelBigApi = mockUtil.createGlobalRestModel();

        String resultBigApi = null;
        try {
            resultBigApi = configActions.validateConfig(restModelBigApi);
            fail();
        } catch (final AlertFieldException e) {
            assertTrue(true);
        }

        assertNull(resultBigApi);
    }

    @Test
    @Override
    public void testValidConfig() throws Exception {
        final GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(null, null, null);

        final String url = "https://www.google.com/";
        final String user = "User";
        final String password = "Password";
        HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setUrl(url);
        serverConfigBuilder.setUsername(user);
        serverConfigBuilder.setUsername(password);

        try {
            configActions.validateBlackDuckConfiguration(serverConfigBuilder);
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
            configActions.validateBlackDuckConfiguration(serverConfigBuilder);
        } catch (final AlertFieldException e) {
            fail();
        }
    }

    @Test
    public void testCreateRestConnection() throws Exception {
        final GlobalBlackDuckConfigActions configActions = new GlobalBlackDuckConfigActions(null, null, null);

        final String url = "https://www.google.com/";
        final String apiToken = "User";
        HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setUrl(url);
        serverConfigBuilder.setApiToken(apiToken);

        // we create this spy to skip the server validation that happens in the build method
        serverConfigBuilder = Mockito.spy(serverConfigBuilder);
        Mockito.doAnswer(new Answer<HubServerConfig>() {
            @Override
            public HubServerConfig answer(final InvocationOnMock invocation) throws Throwable {
                final HubServerConfig hubServerConfig = new HubServerConfig(new URL(url), 0, apiToken, new ProxyInfo(null, 0, null, null, null, null), false);
                return hubServerConfig;
            }
        }).when(serverConfigBuilder).build();

        final RestConnection restConnection = configActions.createRestConnection(serverConfigBuilder);
        assertNotNull(restConnection);
    }

    @Override
    public void testConfigurationChangeTriggers() {

    }

    @Override
    public MockGlobalBlackDuckEntity getGlobalEntityMockUtil() {
        return new MockGlobalBlackDuckEntity();
    }

    @Override
    public MockGlobalBlackDuckRestModel getGlobalRestModelMockUtil() {
        return new MockGlobalBlackDuckRestModel();
    }

}
