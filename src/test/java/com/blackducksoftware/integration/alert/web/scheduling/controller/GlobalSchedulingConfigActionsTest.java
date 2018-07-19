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
package com.blackducksoftware.integration.alert.web.scheduling.controller;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.alert.config.PurgeConfig;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.web.actions.GlobalActionsTest;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingConfigActions;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingConfigRestModel;
import com.blackducksoftware.integration.alert.web.scheduling.mock.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.alert.web.scheduling.model.MockGlobalSchedulingRestModel;

public class GlobalSchedulingConfigActionsTest extends GlobalActionsTest<GlobalSchedulingConfigRestModel, GlobalSchedulingConfigEntity, GlobalSchedulingRepository, GlobalSchedulingConfigActions> {

    @Override
    public GlobalSchedulingConfigActions getMockedConfigActions() {
        return createMockedConfigActionsUsingObjectTransformer(new ObjectTransformer());
    }

    @Override
    public GlobalSchedulingConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final AccumulatorConfig mockedAccumulatorConfig = Mockito.mock(AccumulatorConfig.class);
        Mockito.when(mockedAccumulatorConfig.getMillisecondsToNextRun()).thenReturn(33000l);
        final DailyDigestBatchConfig mockedDailyDigestBatchConfig = Mockito.mock(DailyDigestBatchConfig.class);
        Mockito.when(mockedDailyDigestBatchConfig.getFormatedNextRunTime()).thenReturn("01/19/2018 02:00 AM UTC");
        final PurgeConfig mockedPurgeConfig = Mockito.mock(PurgeConfig.class);
        Mockito.when(mockedPurgeConfig.getFormatedNextRunTime()).thenReturn("01/21/2018 12:00 AM UTC");

        final GlobalSchedulingRepository globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepository.class);

        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedAccumulatorConfig, mockedDailyDigestBatchConfig, mockedPurgeConfig, globalSchedulingRepository, objectTransformer);
        return configActions;
    }

    @Override
    public Class<GlobalSchedulingConfigEntity> getGlobalEntityClass() {
        return GlobalSchedulingConfigEntity.class;
    }

    @Override
    public void testConfigurationChangeTriggers() {
        final AccumulatorConfig mockedAccumulatorConfig = Mockito.mock(AccumulatorConfig.class);
        final DailyDigestBatchConfig mockedDailyDigestBatchConfig = Mockito.mock(DailyDigestBatchConfig.class);
        final PurgeConfig mockedPurgeConfig = Mockito.mock(PurgeConfig.class);

        final GlobalSchedulingRepository globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepository.class);
        Mockito.when(globalSchedulingRepository.findAll()).thenReturn(Arrays.asList(getGlobalEntityMockUtil().createGlobalEntity()));

        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedAccumulatorConfig, mockedDailyDigestBatchConfig, mockedPurgeConfig, globalSchedulingRepository, new ObjectTransformer());
        configActions.configurationChangeTriggers(null);
        Mockito.verify(mockedAccumulatorConfig, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedDailyDigestBatchConfig, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.reset(mockedAccumulatorConfig);
        Mockito.reset(mockedDailyDigestBatchConfig);
        Mockito.reset(mockedPurgeConfig);

        final GlobalSchedulingConfigRestModel restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.configurationChangeTriggers(restModel);
        Mockito.verify(mockedAccumulatorConfig, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedDailyDigestBatchConfig, Mockito.times(1)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(1)).scheduleExecution(Mockito.any());
    }

    @Test
    @Override
    public void testInvalidConfig() {
        final String invalidCron = "invalid";
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, new ObjectTransformer());
        GlobalSchedulingConfigRestModel restModel = new GlobalSchedulingConfigRestModel("1", invalidCron, invalidCron, invalidCron, invalidCron, invalidCron);

        AlertFieldException caughtException = null;
        try {
            configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Must be a number between 0 and 23", caughtException.getFieldErrors().get("dailyDigestHourOfDay"));
        assertEquals("Must be a number between 1 and 7", caughtException.getFieldErrors().get("purgeDataFrequencyDays"));
        assertEquals(2, caughtException.getFieldErrors().size());

        restModel = new GlobalSchedulingConfigRestModel("1", "-1", "-1", "-1", "-1", "-1");

        caughtException = null;
        try {
            configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Must be a number between 0 and 23", caughtException.getFieldErrors().get("dailyDigestHourOfDay"));
        assertEquals("Must be a number between 1 and 7", caughtException.getFieldErrors().get("purgeDataFrequencyDays"));
        assertEquals(2, caughtException.getFieldErrors().size());

        restModel = new GlobalSchedulingConfigRestModel("1", "100000", "100000", "100000", "100000", "100000");

        caughtException = null;
        try {
            configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Must be a number less than 24", caughtException.getFieldErrors().get("dailyDigestHourOfDay"));
        assertEquals("Must be a number less than 8", caughtException.getFieldErrors().get("purgeDataFrequencyDays"));
        assertEquals(2, caughtException.getFieldErrors().size());

        restModel = new GlobalSchedulingConfigRestModel("1", "", "", "", "", "");

        caughtException = null;
        try {
            configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Must be a number between 0 and 23", caughtException.getFieldErrors().get("dailyDigestHourOfDay"));
        assertEquals("Must be a number between 1 and 7", caughtException.getFieldErrors().get("purgeDataFrequencyDays"));
        assertEquals(2, caughtException.getFieldErrors().size());
    }

    @Test
    public void validateConfigWithValidArgsTest() {
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, new ObjectTransformer());
        final GlobalSchedulingConfigRestModel restModel = getGlobalRestModelMockUtil().createGlobalRestModel();

        String validationString = null;
        AlertFieldException caughtException = null;
        try {
            validationString = configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNull(caughtException);
        assertEquals("Valid", validationString);
    }

    @Test
    @Override
    public void testGetConfig() throws Exception {
        Mockito.when(configActions.getRepository().findById(Mockito.anyLong())).thenReturn(Optional.of(getGlobalEntityMockUtil().createGlobalEntity()));
        Mockito.when(configActions.getRepository().findAll()).thenReturn(Arrays.asList(getGlobalEntityMockUtil().createGlobalEntity()));

        // We must mask the rest model because the configActions will have masked those returned by getConfig(...)
        final GlobalSchedulingConfigRestModel restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.maskRestModel(restModel);

        List<GlobalSchedulingConfigRestModel> configsById = configActions.getConfig(1L);
        List<GlobalSchedulingConfigRestModel> allConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allConfigs.size() == 1);

        final GlobalSchedulingConfigRestModel configById = configsById.get(0);
        final GlobalSchedulingConfigRestModel config = allConfigs.get(0);
        assertEquals(restModel, configById);
        assertEquals(restModel, config);

        Mockito.when(configActions.getRepository().findById(Mockito.anyLong())).thenReturn(Optional.empty());
        Mockito.when(configActions.getRepository().findAll()).thenReturn(null);

        configsById = configActions.getConfig(1L);
        allConfigs = configActions.getConfig(null);

        assertNotNull(configsById);
        assertNotNull(allConfigs);
        assertTrue(!configsById.isEmpty());
        assertTrue(!allConfigs.isEmpty());
    }

    @Override
    public MockGlobalSchedulingEntity getGlobalEntityMockUtil() {
        return new MockGlobalSchedulingEntity();
    }

    @Override
    public MockGlobalSchedulingRestModel getGlobalRestModelMockUtil() {
        return new MockGlobalSchedulingRestModel();
    }

}
