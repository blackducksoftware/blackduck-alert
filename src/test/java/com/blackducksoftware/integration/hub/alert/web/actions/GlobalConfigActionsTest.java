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
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class GlobalConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void testDoesConfigExist() {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        Mockito.when(mockedGlobalRepository.exists(Mockito.anyLong())).thenReturn(true);

        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);
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

        final GlobalConfigRestModel restModel = mockUtils.createGlobalConfigRestModel();

        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);
        List<GlobalConfigRestModel> emailConfigsById = configActions.getConfig(1L);
        List<GlobalConfigRestModel> allGlobalConfigs = configActions.getConfig(null);

        assertTrue(emailConfigsById.size() == 1);
        assertTrue(allGlobalConfigs.size() == 1);

        final GlobalConfigRestModel emailConfigById = emailConfigsById.get(0);
        final GlobalConfigRestModel emailConfig = allGlobalConfigs.get(0);
        assertEquals(restModel, emailConfigById);
        assertEquals(restModel, emailConfig);

        Mockito.when(mockedGlobalRepository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockedGlobalRepository.findAll()).thenReturn(null);

        emailConfigsById = configActions.getConfig(1L);
        allGlobalConfigs = configActions.getConfig(null);

        assertNotNull(emailConfigsById);
        assertNotNull(allGlobalConfigs);
        assertTrue(emailConfigsById.isEmpty());
        assertTrue(allGlobalConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);

        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);
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
        final GlobalConfigEntity expectedGlobalConfigEntity = mockUtils.createGlobalConfigEntity();
        Mockito.when(mockedGlobalRepository.save(Mockito.any(GlobalConfigEntity.class))).thenReturn(expectedGlobalConfigEntity);

        GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);

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
        configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, transformer);

        emailConfigEntity = configActions.saveConfig(mockUtils.createGlobalConfigRestModel());
        assertNull(emailConfigEntity);
    }

    // @Test
    public void testValidateConfig() throws Exception {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);

        String response = configActions.validateConfig(mockUtils.createGlobalConfigRestModel());
        assertEquals("Valid", response);

        final GlobalConfigRestModel restModel = new GlobalConfigRestModel("1", "HubUrl", "NotInteger", "HubUsername", "HubPassword", "HubProxyHost", "HubProxyPort", "HubProxyUsername", "HubProxyPassword", "NotBoolean", "AccumulatorCron",
                "DailyDigestCron");

        final Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("hubTimeout", "Not an Integer.");
        fieldErrors.put("hubAlwaysTrustCertificate", "Not an Boolean.");
        try {
            response = configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            for (final Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                assertTrue(fieldErrors.containsKey(entry.getKey()));
                final String expectedValue = fieldErrors.get(entry.getKey());
                assertEquals(expectedValue, entry.getValue());
            }
        }
    }

    @Test
    public void testTestConfig() throws Exception {
        final GlobalRepository mockedGlobalRepository = Mockito.mock(GlobalRepository.class);
        final RestConnection mockedRestConnection = Mockito.mock(RestConnection.class);
        GlobalConfigActions configActions = new GlobalConfigActions(mockedGlobalRepository, null, null, objectTransformer);
        configActions = Mockito.spy(configActions);
        // Mockito.when(configActions.creat)

        // configActions.testConfig(mockUtils.createGlobalConfigRestModel());
        // verify(mockedGlobalChannel, times(1)).testMessage(Mockito.any());
        // Mockito.reset(mockedGlobalChannel);
        //
        // configActions.testConfig(null);
        // verify(mockedGlobalChannel, times(1)).testMessage(Mockito.any());
    }

    @Test
    public void testConfigurationChangeTriggers() {
        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, null, null);
        configActions.configurationChangeTriggers(null);
    }

    @Test
    public void testIsBoolean() {
        final GlobalConfigActions configActions = new GlobalConfigActions(null, null, null, null);
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
