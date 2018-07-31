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

import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.web.actions.GlobalActionsTest;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingConfig;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingConfigActions;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingContentConverter;
import com.blackducksoftware.integration.alert.web.scheduling.mock.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.alert.web.scheduling.model.MockGlobalSchedulingRestModel;
import com.blackducksoftware.integration.alert.workflow.scheduled.PurgeTask;
import com.blackducksoftware.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.blackducksoftware.integration.alert.workflow.scheduled.frequency.OnDemandTask;

public class GlobalSchedulingConfigActionsTest extends GlobalActionsTest<GlobalSchedulingConfig, GlobalSchedulingConfigEntity, GlobalSchedulingRepository, GlobalSchedulingConfigActions> {

    @Override
    public GlobalSchedulingConfigActions getMockedConfigActions() {
        final DailyTask mockedDailyTask = Mockito.mock(DailyTask.class);
        final OnDemandTask mockedOnDemandTask = Mockito.mock(OnDemandTask.class);
        Mockito.when(mockedDailyTask.getFormatedNextRunTime()).thenReturn(Optional.of("01/19/2018 02:00 AM UTC"));
        Mockito.when(mockedOnDemandTask.getMillisecondsToNextRun()).thenReturn(Optional.of(33000l));
        final PurgeTask mockedPurgeConfig = Mockito.mock(PurgeTask.class);
        Mockito.when(mockedPurgeConfig.getFormatedNextRunTime()).thenReturn(Optional.of("01/21/2018 12:00 AM UTC"));

        final GlobalSchedulingRepository globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepository.class);

        final GlobalSchedulingContentConverter globalSchedulingContentConverter = new GlobalSchedulingContentConverter(getContentConverter());
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedDailyTask, mockedOnDemandTask, mockedPurgeConfig, globalSchedulingRepository, globalSchedulingContentConverter);
        return configActions;
    }

    @Override
    public Class<GlobalSchedulingConfigEntity> getGlobalEntityClass() {
        return GlobalSchedulingConfigEntity.class;
    }

    @Override
    public void testConfigurationChangeTriggers() {
        final DailyTask mockedDailyTask = Mockito.mock(DailyTask.class);
        final OnDemandTask mockedOnDemandTask = Mockito.mock(OnDemandTask.class);

        final PurgeTask mockedPurgeConfig = Mockito.mock(PurgeTask.class);

        final GlobalSchedulingRepository globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepository.class);
        Mockito.when(globalSchedulingRepository.findAll()).thenReturn(Arrays.asList(getGlobalEntityMockUtil().createGlobalEntity()));

        final GlobalSchedulingContentConverter globalSchedulingContentConverter = new GlobalSchedulingContentConverter(getContentConverter());
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedDailyTask, mockedOnDemandTask, mockedPurgeConfig, globalSchedulingRepository, globalSchedulingContentConverter);
        configActions.configurationChangeTriggers(null);
        Mockito.verify(mockedDailyTask, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(0)).scheduleExecution(Mockito.any());
        Mockito.reset(mockedDailyTask);
        Mockito.reset(mockedPurgeConfig);

        final GlobalSchedulingConfig restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.configurationChangeTriggers(restModel);
        Mockito.verify(mockedDailyTask, Mockito.times(1)).scheduleExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(1)).scheduleExecution(Mockito.any());
    }

    @Test
    @Override
    public void testInvalidConfig() {
        final String invalidCron = "invalid";
        final GlobalSchedulingContentConverter globalSchedulingContentConverter = new GlobalSchedulingContentConverter(getContentConverter());
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, globalSchedulingContentConverter);
        GlobalSchedulingConfig restModel = new GlobalSchedulingConfig("1", invalidCron, invalidCron, invalidCron, invalidCron, invalidCron);

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

        restModel = new GlobalSchedulingConfig("1", "-1", "-1", "-1", "-1", "-1");

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

        restModel = new GlobalSchedulingConfig("1", "100000", "100000", "100000", "100000", "100000");

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

        restModel = new GlobalSchedulingConfig("1", "", "", "", "", "");

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
        final GlobalSchedulingContentConverter globalSchedulingContentConverter = new GlobalSchedulingContentConverter(getContentConverter());
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, globalSchedulingContentConverter);
        final GlobalSchedulingConfig restModel = getGlobalRestModelMockUtil().createGlobalRestModel();

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
        final GlobalSchedulingConfig restModel = getGlobalRestModelMockUtil().createGlobalRestModel();
        configActions.maskRestModel(restModel);

        List<GlobalSchedulingConfig> configsById = configActions.getConfig(1L);
        List<GlobalSchedulingConfig> allConfigs = configActions.getConfig(null);

        assertTrue(configsById.size() == 1);
        assertTrue(allConfigs.size() == 1);

        final GlobalSchedulingConfig configById = configsById.get(0);
        final GlobalSchedulingConfig config = allConfigs.get(0);
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
