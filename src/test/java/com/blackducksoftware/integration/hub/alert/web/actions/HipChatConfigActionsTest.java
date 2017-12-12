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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.DistributionMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.google.gson.Gson;

public class HipChatConfigActionsTest {
    final HipChatMockUtils mockUtils = new HipChatMockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(mockedHipChatRepository.exists(Mockito.anyLong())).thenReturn(true);

        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final Gson gson = new Gson();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(mockedHipChatRepository.exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws AlertException {
        final DistributionMockUtils distMockUtils = new DistributionMockUtils();
        final CommonDistributionConfigEntity commonDistributionConfigEntity = distMockUtils.createDistributionConfigEntity();
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonRepository.findByDistributionConfigIdAndDistributionType(Mockito.any(), Mockito.any())).thenReturn(commonDistributionConfigEntity);
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createEntity());
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createEntity()));

        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final Gson gson = new Gson();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);

        // We must mask the rest model because the configActions will have masked those returned by getConfig(...)
        final HipChatDistributionRestModel restModel = mockUtils.createRestModel();
        configActions.maskRestModel(restModel);

        List<HipChatDistributionRestModel> configsById = configActions.getConfig(1L);
        List<HipChatDistributionRestModel> allHipChatConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allHipChatConfigs.size() == 1);

        final HipChatDistributionRestModel configById = configsById.get(0);
        final HipChatDistributionRestModel hipChatConfig = allHipChatConfigs.get(0);

        assertEquals(restModel, configById);
        assertEquals(restModel, hipChatConfig);

        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(Arrays.asList());

        configsById = configActions.getConfig(1L);
        allHipChatConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allHipChatConfigs);
        assertTrue(configsById.isEmpty());
        assertTrue(allHipChatConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createDistributionConfigEntity());
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final Gson gson = new Gson();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);
        configActions.deleteConfig(1L);
        verify(mockedHipChatRepository, times(1)).delete(Mockito.anyLong());

        Mockito.reset(mockedHipChatRepository);
        configActions.deleteConfig("1");
        verify(mockedHipChatRepository, times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;
        Mockito.reset(mockedHipChatRepository);
        configActions.deleteConfig(idLong);
        verify(mockedHipChatRepository, times(0)).delete(Mockito.anyLong());

        Mockito.reset(mockedHipChatRepository);
        configActions.deleteConfig(idString);
        verify(mockedHipChatRepository, times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        final HipChatDistributionConfigEntity expectedHipChatConfigEntity = mockUtils.createEntity();
        Mockito.when(mockedHipChatRepository.save(Mockito.any(HipChatDistributionConfigEntity.class))).thenReturn(expectedHipChatConfigEntity);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);

        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final Gson gson = new Gson();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);
        HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);

        HipChatDistributionConfigEntity hipChatConfigEntity = configActions.saveConfig(mockUtils.createRestModel());
        assertNotNull(hipChatConfigEntity);
        assertEquals(expectedHipChatConfigEntity, hipChatConfigEntity);

        hipChatConfigEntity = configActions.saveConfig(null);
        assertNull(hipChatConfigEntity);

        Mockito.when(mockedHipChatRepository.save(Mockito.any(HipChatDistributionConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            hipChatConfigEntity = configActions.saveConfig(mockUtils.createRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, transformer, hipChatChannel);

        hipChatConfigEntity = configActions.saveConfig(mockUtils.createRestModel());
        assertNull(hipChatConfigEntity);
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(null, null, null, null);
        configActions.configurationChangeTriggers(null);
    }

    @Test
    public void testIsBoolean() {
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(null, null, null, null);
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

    @Test
    public void testConstructRestModel() throws AlertException {
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final Gson gson = new Gson();
        final HipChatChannel hipChatChannel = new HipChatChannel(gson, globalProperties, null, null, null);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);

        final DistributionMockUtils distributionMockUtils = new DistributionMockUtils();
        final CommonDistributionConfigEntity commonEntity = distributionMockUtils.createDistributionConfigEntity();

        final HipChatDistributionRestModel actualRestModel = configActions.constructRestModel(commonEntity, mockUtils.createEntity());
        assertEquals(mockUtils.createRestModel(), actualRestModel);
    }
}
