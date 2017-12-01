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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

public class HipChatConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        Mockito.when(mockedHipChatRepository.exists(Mockito.anyLong())).thenReturn(true);

        final HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, null);
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
        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createHipChatConfigEntity());
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createHipChatConfigEntity()));

        final HipChatConfigRestModel restModel = mockUtils.createHipChatConfigRestModel();

        final HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, null);
        List<HipChatConfigRestModel> emailConfigsById = configActions.getConfig(1L);
        List<HipChatConfigRestModel> allHipChatConfigs = configActions.getConfig(null);

        assertTrue(emailConfigsById.size() == 1);
        assertTrue(allHipChatConfigs.size() == 1);

        final HipChatConfigRestModel emailConfigById = emailConfigsById.get(0);
        final HipChatConfigRestModel emailConfig = allHipChatConfigs.get(0);
        assertEquals(restModel, emailConfigById);
        assertEquals(restModel, emailConfig);

        Mockito.when(mockedHipChatRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedHipChatRepository.findAll()).thenReturn(null);

        emailConfigsById = configActions.getConfig(1L);
        allHipChatConfigs = configActions.getConfig(null);

        assertNotNull(emailConfigsById);
        assertNotNull(allHipChatConfigs);
        assertTrue(emailConfigsById.isEmpty());
        assertTrue(allHipChatConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);

        final HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, null);
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
        final GlobalHipChatConfigEntity expectedHipChatConfigEntity = mockUtils.createHipChatConfigEntity();
        Mockito.when(mockedHipChatRepository.save(Mockito.any(GlobalHipChatConfigEntity.class))).thenReturn(expectedHipChatConfigEntity);

        HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, null);

        GlobalHipChatConfigEntity emailConfigEntity = configActions.saveConfig(mockUtils.createHipChatConfigRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedHipChatConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(mockedHipChatRepository.save(Mockito.any(GlobalHipChatConfigEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(mockUtils.createHipChatConfigRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new HipChatConfigActions(mockedHipChatRepository, transformer, null);

        emailConfigEntity = configActions.saveConfig(mockUtils.createHipChatConfigRestModel());
        assertNull(emailConfigEntity);
    }

    @Test
    public void testValidateConfig() throws Exception {
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        final HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, null);

        String response = configActions.validateConfig(mockUtils.createHipChatConfigRestModel());
        assertEquals("Valid", response);

        final HipChatConfigRestModel restModel = new HipChatConfigRestModel("1", "ApiKey", "NotInteger", "NotABoolean", "Color");

        final Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("roomId", "Not an Integer.");
        fieldErrors.put("notify", "Not an Boolean.");
        try {
            response = configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            for (final Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                assertTrue(fieldErrors.containsKey(entry.getKey()));
                final String expectedValue = fieldErrors.get(entry.getKey());
                assertEquals(expectedValue, entry.getValue());
            }
        }

        response = configActions.validateConfig(new HipChatConfigRestModel());
        assertEquals("Valid", response);
    }

    @Test
    public void testChannelTestConfig() throws Exception {
        final HipChatChannel mockedHipChatChannel = Mockito.mock(HipChatChannel.class);
        Mockito.when(mockedHipChatChannel.testMessage(Mockito.any())).thenReturn("");
        final GlobalHipChatRepository mockedHipChatRepository = Mockito.mock(GlobalHipChatRepository.class);
        final HipChatConfigActions configActions = new HipChatConfigActions(mockedHipChatRepository, objectTransformer, mockedHipChatChannel);

        configActions.channelTestConfig(mockUtils.createHipChatConfigRestModel());
        verify(mockedHipChatChannel, times(1)).testMessage(Mockito.any());
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final HipChatConfigActions configActions = new HipChatConfigActions(null, null, null);
        configActions.configurationChangeTriggers(null);
    }

    @Test
    public void testIsBoolean() {
        final HipChatConfigActions configActions = new HipChatConfigActions(null, null, null);
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
