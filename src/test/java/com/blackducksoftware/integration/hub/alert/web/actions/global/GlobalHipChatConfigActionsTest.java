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
package com.blackducksoftware.integration.hub.alert.web.actions.global;

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

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

public class GlobalHipChatConfigActionsTest {
    private final HipChatMockUtils mockUtils = new HipChatMockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        Mockito.when(mockedHipChatRepository.exists(Mockito.anyLong())).thenReturn(true);

        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, objectTransformer);
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
    public void testGetConfig() throws Exception {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createGlobalEntity());
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createGlobalEntity()));
        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, objectTransformer);

        // We must mask the rest model because the configActions will have masked those returned by getConfig(...)
        final GlobalHipChatConfigRestModel restModel = mockUtils.createGlobalRestModel();
        configActions.maskRestModel(restModel);

        List<GlobalHipChatConfigRestModel> configsById = configActions.getConfig(1L);
        List<GlobalHipChatConfigRestModel> allHipChatConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allHipChatConfigs.size() == 1);

        final GlobalHipChatConfigRestModel configById = configsById.get(0);
        final GlobalHipChatConfigRestModel hipChatConfig = allHipChatConfigs.get(0);
        assertEquals(restModel, configById);
        assertEquals(restModel, hipChatConfig);

        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(null);

        configsById = configActions.getConfig(1L);
        allHipChatConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allHipChatConfigs);
        assertTrue(configsById.isEmpty());
        assertTrue(allHipChatConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);

        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, objectTransformer);
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
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        final GlobalHipChatConfigEntity expectedHipChatConfigEntity = mockUtils.createGlobalEntity();
        Mockito.when(mockedHipChatRepository.save(Mockito.any(GlobalHipChatConfigEntity.class))).thenReturn(expectedHipChatConfigEntity);

        GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, objectTransformer);

        GlobalHipChatConfigEntity emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedHipChatConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(mockedHipChatRepository.save(Mockito.any(GlobalHipChatConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, transformer);

        emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalRestModel());
        assertNull(emailConfigEntity);
    }

    // TODO fix when we have decided on an implementation of GlobalEmailConfigActions.testConfig()
    // @Test
    public void testChannelTestConfig() throws Exception {
        final HipChatChannel mockedHipChatChannel = Mockito.mock(HipChatChannel.class);
        Mockito.when(mockedHipChatChannel.testMessage(Mockito.any())).thenReturn("");
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(mockedHipChatRepository, objectTransformer);

        configActions.channelTestConfig(mockUtils.createGlobalRestModel());
        verify(mockedHipChatChannel, times(1)).testMessage(Mockito.any());
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(null, null);
        configActions.configurationChangeTriggers(null);
    }

    @Test
    public void testIsBoolean() {
        final GlobalHipChatConfigActions configActions = new GlobalHipChatConfigActions(null, null);
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
