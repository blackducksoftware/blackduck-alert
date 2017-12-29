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

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class GlobalActionsTest<GR extends ConfigRestModel, GE extends DatabaseEntity, GCA extends ConfigActions<GE, GR>> {
    protected GCA configActions;

    public GlobalActionsTest() {
        configActions = getMockedConfigActions();
    }

    public abstract MockUtils<?, GR, ?, GE> getMockUtil();

    public abstract GCA getMockedConfigActions();

    @Test
    public void testDoesConfigExist() {
        Mockito.when(configActions.repository.exists(Mockito.anyLong())).thenReturn(true);
        assertTrue(configActions.doesConfigExist(1L));
        assertTrue(configActions.doesConfigExist("1"));

        Mockito.when(configActions.repository.exists(Mockito.anyLong())).thenReturn(false);
        assertFalse(configActions.doesConfigExist(1L));
        assertFalse(configActions.doesConfigExist("1"));

        final String idString = null;
        final Long idLong = null;
        assertFalse(configActions.doesConfigExist(idString));
        assertFalse(configActions.doesConfigExist(idLong));
    }

    @Test
    public void testGetConfig() throws Exception {
        Mockito.when(configActions.repository.findOne(Mockito.anyLong())).thenReturn(getMockUtil().createGlobalEntity());
        Mockito.when(configActions.repository.findAll()).thenReturn(Arrays.asList(getMockUtil().createGlobalEntity()));

        // We must mask the rest model because the configActions will have masked those returned by getConfig(...)
        final GR restModel = getMockUtil().createGlobalRestModel();
        configActions.maskRestModel(restModel);

        List<GR> configsById = configActions.getConfig(1L);
        List<GR> allConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allConfigs.size() == 1);

        final GR configById = configsById.get(0);
        final GR config = allConfigs.get(0);
        assertEquals(restModel, configById);
        assertEquals(restModel, config);

        Mockito.when(configActions.repository.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(configActions.repository.findAll()).thenReturn(null);

        configsById = configActions.getConfig(1L);
        allConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allConfigs);
        assertTrue(configsById.isEmpty());
        assertTrue(allConfigs.isEmpty());
    }

    @Test
    public void testDeleteConfig() {
        configActions.deleteConfig(1L);
        verify(configActions.repository, times(1)).delete(Mockito.anyLong());

        Mockito.reset(configActions.repository);
        configActions.deleteConfig("1");
        verify(configActions.repository, times(1)).delete(Mockito.anyLong());

        final String idString = null;
        final Long idLong = null;
        Mockito.reset(configActions.repository);
        configActions.deleteConfig(idLong);
        verify(configActions.repository, times(0)).delete(Mockito.anyLong());

        Mockito.reset(configActions.repository);
        configActions.deleteConfig(idString);
        verify(configActions.repository, times(0)).delete(Mockito.anyLong());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final GE expectedHipChatConfigEntity = getMockUtil().createGlobalEntity();
        Mockito.when(configActions.repository.save(Mockito.any(getGlobalEntityClass()))).thenReturn(expectedHipChatConfigEntity);

        GE emailConfigEntity = configActions.saveConfig(getMockUtil().createGlobalRestModel());
        assertNotNull(emailConfigEntity);
        assertEquals(expectedHipChatConfigEntity, emailConfigEntity);

        emailConfigEntity = configActions.saveConfig(null);
        assertNull(emailConfigEntity);

        Mockito.when(configActions.repository.save(Mockito.any(getGlobalEntityClass()))).thenThrow(new RuntimeException("test"));
        try {
            emailConfigEntity = configActions.saveConfig(getMockUtil().createGlobalRestModel());
            fail();
        } catch (final AlertException e) {
            assertEquals("test", e.getMessage());
        }

        final ObjectTransformer transformer = Mockito.mock(ObjectTransformer.class);
        Mockito.when(transformer.configRestModelToDatabaseEntity(Mockito.any(), Mockito.any())).thenReturn(null);
        configActions = createMockedConfigActionsUsingObjectTransformer(transformer);

        emailConfigEntity = configActions.saveConfig(getMockUtil().createGlobalRestModel());
        assertNull(emailConfigEntity);
    }

    public abstract GCA createMockedConfigActionsUsingObjectTransformer(ObjectTransformer objectTransformer);

    public abstract Class<GE> getGlobalEntityClass();

    @Test
    public abstract void testConfigurationChangeTriggers();

    @Test
    public void testIsBoolean() {
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
