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

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigRestModel;

public class HubUsersConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final HubUsersRepository mockedHubUsersRepository = Mockito.mock(HubUsersRepository.class);
        Mockito.when(mockedHubUsersRepository.exists(Mockito.anyLong())).thenReturn(true);

        final HubUsersConfigActions configActions = new HubUsersConfigActions(mockedHubUsersRepository, objectTransformer);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(mockedHubUsersRepository.exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws Exception {
        final HubUsersRepository mockedHubUsersRepository = Mockito.mock(HubUsersRepository.class);
        Mockito.when(mockedHubUsersRepository.findOne(Mockito.anyLong())).thenReturn(mockUtils.createHubUsersEntity());
        Mockito.when(mockedHubUsersRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createHubUsersEntity()));

        final HubUsersConfigRestModel restModel = mockUtils.createHubUsersRestModel();

        final HubUsersConfigActions configActions = new HubUsersConfigActions(mockedHubUsersRepository, objectTransformer);
        List<HubUsersConfigRestModel> configsById = configActions.getConfig(1L);
        List<HubUsersConfigRestModel> allConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allConfigs.size() == 1);

        final HubUsersConfigRestModel configById = configsById.get(0);
        final HubUsersConfigRestModel config = allConfigs.get(0);
        assertEquals(restModel, configById);
        assertEquals(restModel, config);

        Mockito.when(mockedHubUsersRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedHubUsersRepository.findAll()).thenReturn(null);

        configsById = configActions.getConfig(1L);
        allConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allConfigs);
        assertTrue(configsById.isEmpty());
        assertTrue(allConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final HubUsersRepository mockHubUsersRepository = Mockito.mock(HubUsersRepository.class);

        final HubUsersConfigActions configActions = new HubUsersConfigActions(mockHubUsersRepository, objectTransformer);
        configActions.deleteConfig(1L);
        verify(mockHubUsersRepository, times(1)).delete(Mockito.anyLong());

        Mockito.reset(mockHubUsersRepository);
        configActions.deleteConfig("1");
        verify(mockHubUsersRepository, times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;
        Mockito.reset(mockHubUsersRepository);
        configActions.deleteConfig(idLong);
        verify(mockHubUsersRepository, times(0)).delete(Mockito.anyLong());

        Mockito.reset(mockHubUsersRepository);
        configActions.deleteConfig(idString);
        verify(mockHubUsersRepository, times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final HubUsersRepository mockedHubUserRepository = Mockito.mock(HubUsersRepository.class);
        final HubUsersEntity expectedHubUsersConfigEntity = mockUtils.createHubUsersEntity();
        Mockito.when(mockedHubUserRepository.save(Mockito.any(HubUsersEntity.class))).thenReturn(expectedHubUsersConfigEntity);

        HubUsersConfigActions configActions = new HubUsersConfigActions(mockedHubUserRepository, objectTransformer);

        HubUsersEntity configEntity = configActions.saveConfig(mockUtils.createHubUsersRestModel());
        assertNotNull(configEntity);
        assertEquals(expectedHubUsersConfigEntity, configEntity);

        configEntity = configActions.saveConfig(null);
        assertNull(configEntity);

        Mockito.when(mockedHubUserRepository.save(Mockito.any(HubUsersEntity.class))).thenThrow(new RuntimeException("test"));
        try {
            configEntity = configActions.saveConfig(mockUtils.createHubUsersRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = new HubUsersConfigActions(mockedHubUserRepository, transformer);

        configEntity = configActions.saveConfig(mockUtils.createHubUsersRestModel());
        assertNull(configEntity);
    }

    @Test
    public void testValidateConfig() throws Exception {
        final HubUsersRepository mockedHubUsersRepository = Mockito.mock(HubUsersRepository.class);
        final HubUsersConfigActions configActions = new HubUsersConfigActions(mockedHubUsersRepository, objectTransformer);

        String response = configActions.validateConfig(mockUtils.createHubUsersRestModel());
        assertEquals("Valid", response);

        final HubUsersConfigRestModel restModel = new HubUsersConfigRestModel("1", null);

        final Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("username", "Not specified.");
        try {
            response = configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            for (final Map.Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                assertTrue(fieldErrors.containsKey(entry.getKey()));
                final String expectedValue = fieldErrors.get(entry.getKey());
                assertEquals(expectedValue, entry.getValue());
            }
        }
    }

    @Test
    public void testChannelTestConfig() throws Exception {
        // TODO test config when implemented
    }

    @Test
    public void testConfigurationChangeTriggers() {
        // TODO test config change triggers when implemented
    }

}
