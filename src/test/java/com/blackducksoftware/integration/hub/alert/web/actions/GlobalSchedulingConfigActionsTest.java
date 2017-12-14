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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalSchedulingConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;

public class GlobalSchedulingConfigActionsTest {
    private final MockUtils mockUtils = new MockUtils();
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void validateConfigWithInvalidArgsTest() {
        final String invalidCron = "invalid";
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, objectTransformer);
        final GlobalSchedulingConfigRestModel restModel = new GlobalSchedulingConfigRestModel("1", invalidCron, invalidCron, invalidCron);

        AlertFieldException caughtException = null;
        try {
            configActions.validateConfig(restModel);
        } catch (final AlertFieldException e) {
            caughtException = e;
        }
        assertNotNull(caughtException);
        assertEquals("Cron expression must consist of 6 fields (found 1 in \"" + invalidCron + "\")", caughtException.getFieldErrors().get("accumulatorCron"));
        assertEquals("Cron expression must consist of 6 fields (found 1 in \"" + invalidCron + "\")", caughtException.getFieldErrors().get("dailyDigestCron"));
        assertEquals("Cron expression must consist of 6 fields (found 1 in \"" + invalidCron + "\")", caughtException.getFieldErrors().get("purgeDataCron"));
    }

    @Test
    public void validateConfigWithValidArgsTest() {
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, objectTransformer);
        final GlobalSchedulingConfigRestModel restModel = mockUtils.createGlobalSchedulingConfigRestModel();

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
    public void configurationChangeTriggersTest() {
        final AccumulatorConfig mockedAccumulatorConfig = mock(AccumulatorConfig.class);
        final DailyDigestBatchConfig mockedDailyDigestBatchConfig = mock(DailyDigestBatchConfig.class);
        final PurgeConfig mockedPurgeConfig = mock(PurgeConfig.class);

        final GlobalSchedulingRepository globalSchedulingRepository = mock(GlobalSchedulingRepository.class);
        when(globalSchedulingRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createGlobalSchedulingConfigEntity()));
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedAccumulatorConfig, mockedDailyDigestBatchConfig, mockedPurgeConfig, globalSchedulingRepository, objectTransformer);
        configActions.configurationChangeTriggers(null);
        verify(mockedAccumulatorConfig, times(0)).scheduleJobExecution(Mockito.any());
        verify(mockedDailyDigestBatchConfig, times(0)).scheduleJobExecution(Mockito.any());
        verify(mockedPurgeConfig, times(0)).scheduleJobExecution(Mockito.any());
        reset(mockedAccumulatorConfig);
        reset(mockedDailyDigestBatchConfig);
        reset(mockedPurgeConfig);

        final GlobalSchedulingConfigRestModel restModel = mockUtils.createGlobalSchedulingConfigRestModel();
        configActions.configurationChangeTriggers(restModel);
        verify(mockedAccumulatorConfig, times(1)).scheduleJobExecution(Mockito.any());
        verify(mockedDailyDigestBatchConfig, times(1)).scheduleJobExecution(Mockito.any());
        verify(mockedPurgeConfig, times(1)).scheduleJobExecution(Mockito.any());
    }

}
