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
package com.blackducksoftware.integration.hub.alert.hub.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigActions;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubEntity;
import com.blackducksoftware.integration.hub.alert.hub.mock.MockGlobalHubRestModel;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalActionsTest;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigValidator;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UriCombiner;
import com.blackducksoftware.integration.validator.ValidationResults;

public class GlobalHubConfigActionsTest extends GlobalActionsTest<GlobalHubConfigRestModel, GlobalHubConfigEntity, GlobalHubRepositoryWrapper, GlobalHubConfigActions> {

    @Override
    public GlobalHubConfigActions getMockedConfigActions() {
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final GlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);

        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, new ObjectTransformer());
        return configActions;
    }

    @Override
    public GlobalHubConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final GlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);

        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
        return configActions;
    }

    @Override
    public Class<GlobalHubConfigEntity> getGlobalEntityClass() {
        return GlobalHubConfigEntity.class;
    }

    @Test
    @Override
    public void testGetConfig() throws Exception {
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        Mockito.when(mockedGlobalRepository.findOne(Mockito.anyLong())).thenReturn(getGlobalEntityMockUtil().createGlobalEntity());
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(Arrays.asList(getGlobalEntityMockUtil().createGlobalEntity()));
        final GlobalHubConfigEntity databaseEntity = getGlobalEntityMockUtil().createGlobalEntity();
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        globalProperties.setHubTrustCertificate(null);
        globalProperties.setHubUrl(null);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, objectTransformer);
        final GlobalHubConfigRestModel defaultRestModel = objectTransformer.databaseEntityToConfigRestModel(databaseEntity, GlobalHubConfigRestModel.class);
        final GlobalHubConfigRestModel maskedRestModel = configActions.maskRestModel(defaultRestModel);
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
        assertEquals(maskedRestModel.getHubAlwaysTrustCertificate(), environmentGlobalConfig.getHubAlwaysTrustCertificate());
        assertNull(environmentGlobalConfig.getHubApiKey());
        assertEquals(maskedRestModel.getHubProxyHost(), environmentGlobalConfig.getHubProxyHost());
        assertNull(environmentGlobalConfig.getHubProxyPassword());
        assertEquals(maskedRestModel.getHubProxyPort(), environmentGlobalConfig.getHubProxyPort());
        assertEquals(maskedRestModel.getHubProxyUsername(), environmentGlobalConfig.getHubProxyUsername());
        assertNull(environmentGlobalConfig.getHubTimeout());
        assertEquals(maskedRestModel.getHubUrl(), environmentGlobalConfig.getHubUrl());
        assertNull(environmentGlobalConfig.getId());
    }

    @Test
    public void testTestConfig() throws Exception {
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, new ObjectTransformer());

        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(getGlobalRestModelMockUtil().createGlobalRestModel());
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel fullRestModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.testConfig(fullRestModel);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        final GlobalHubConfigRestModel partialRestModel = configActions.maskRestModel(restModel);

        Mockito.doAnswer(new Answer<GlobalHubConfigEntity>() {
            @Override
            public GlobalHubConfigEntity answer(final InvocationOnMock invocation) throws Throwable {
                return getGlobalEntityMockUtil().createGlobalEntity();
            }
        }).when(mockedGlobalRepository).findOne(Mockito.anyLong());

        final String result = configActions.testConfig(partialRestModel);
        assertEquals("Successfully connected to the Hub.", result);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();

    }

    @Test
    @Override
    public void testChannelTestConfig() throws Exception {
        final MockGlobalHubRestModel mockUtils = new MockGlobalHubRestModel();
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        final GlobalHubRepositoryWrapper mockedGlobalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        GlobalHubConfigActions configActions = new GlobalHubConfigActions(mockedGlobalRepository, globalProperties, new ObjectTransformer());
        configActions = Mockito.spy(configActions);
        Mockito.doAnswer(new Answer<RestConnection>() {
            @Override
            public RestConnection answer(final InvocationOnMock invocation) throws Throwable {
                return mockedRestConnection;
            }
        }).when(configActions).createRestConnection(Mockito.any(HubServerConfigBuilder.class));

        Mockito.doNothing().when(configActions).validateHubConfiguration(Mockito.any(HubServerConfigBuilder.class));

        configActions.testConfig(mockUtils.createGlobalRestModel());
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
        Mockito.reset(mockedRestConnection);

        final GlobalHubConfigRestModel restModel = mockUtils.createGlobalRestModel();
        final String result = configActions.channelTestConfig(restModel);
        assertEquals("Successfully connected to the Hub.", result);
        Mockito.verify(mockedRestConnection, Mockito.times(1)).connect();
    }

    @Override
    public void testInvalidConfig() {
        final MockGlobalHubRestModel mockUtil = new MockGlobalHubRestModel();
        mockUtil.setHubTimeout("qqq");
        final GlobalHubConfigRestModel restModel = mockUtil.createGlobalRestModel();

        String result = null;
        try {
            result = configActions.validateConfig(restModel);
            fail();
        } catch (final AlertFieldException e) {
            assertTrue(true);
        }

        assertNull(result);

        mockUtil.setHubApiKey(StringUtils.repeat('a', 300));
        final GlobalHubConfigRestModel restModelBigApi = mockUtil.createGlobalRestModel();

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
        final String apiToken = "User";
        HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setHubUrl(url);
        serverConfigBuilder.setApiToken(apiToken);

        // we create this spy to skip the server validation that happens in the build method
        serverConfigBuilder = Mockito.spy(serverConfigBuilder);
        Mockito.doAnswer(new Answer<HubServerConfig>() {
            @Override
            public HubServerConfig answer(final InvocationOnMock invocation) throws Throwable {
                final HubServerConfig hubServerConfig = new HubServerConfig(new URL(url), 0, apiToken, new ProxyInfo(null, 0, null, null, null, null), false, new UriCombiner());
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
    public MockGlobalHubEntity getGlobalEntityMockUtil() {
        return new MockGlobalHubEntity();
    }

    @Override
    public MockGlobalHubRestModel getGlobalRestModelMockUtil() {
        return new MockGlobalHubRestModel();
    }

}
