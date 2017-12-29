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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.mock.GlobalSchedulingMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;

public class GlobalSchedulingConfigActionsTest extends GlobalActionsTest<GlobalSchedulingConfigRestModel, GlobalSchedulingConfigEntity, GlobalSchedulingConfigActions> {
    private final GlobalSchedulingMockUtils mockUtils = new GlobalSchedulingMockUtils();

    @Override
    public GlobalSchedulingConfigActions getMockedConfigActions() {
        return createMockedConfigActionsUsingObjectTransformer(new ObjectTransformer());
    }

    @Override
    public GlobalSchedulingConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final AccumulatorConfig mockedAccumulatorConfig = Mockito.mock(AccumulatorConfig.class);
        final DailyDigestBatchConfig mockedDailyDigestBatchConfig = Mockito.mock(DailyDigestBatchConfig.class);
        final PurgeConfig mockedPurgeConfig = Mockito.mock(PurgeConfig.class);
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
        Mockito.when(globalSchedulingRepository.findAll()).thenReturn(Arrays.asList(mockUtils.createGlobalEntity()));
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(mockedAccumulatorConfig, mockedDailyDigestBatchConfig, mockedPurgeConfig, globalSchedulingRepository, new ObjectTransformer());
        configActions.configurationChangeTriggers(null);
        Mockito.verify(mockedAccumulatorConfig, Mockito.times(0)).scheduleJobExecution(Mockito.any());
        Mockito.verify(mockedDailyDigestBatchConfig, Mockito.times(0)).scheduleJobExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(0)).scheduleJobExecution(Mockito.any());
        Mockito.reset(mockedAccumulatorConfig);
        Mockito.reset(mockedDailyDigestBatchConfig);
        Mockito.reset(mockedPurgeConfig);

        final GlobalSchedulingConfigRestModel restModel = mockUtils.createGlobalRestModel();
        configActions.configurationChangeTriggers(restModel);
        Mockito.verify(mockedAccumulatorConfig, Mockito.times(1)).scheduleJobExecution(Mockito.any());
        Mockito.verify(mockedDailyDigestBatchConfig, Mockito.times(1)).scheduleJobExecution(Mockito.any());
        Mockito.verify(mockedPurgeConfig, Mockito.times(1)).scheduleJobExecution(Mockito.any());
    }

    @Test
    public void validateConfigWithInvalidArgsTest() {
        final String invalidCron = "invalid";
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, new ObjectTransformer());
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
        final GlobalSchedulingConfigActions configActions = new GlobalSchedulingConfigActions(null, null, null, null, new ObjectTransformer());
        final GlobalSchedulingConfigRestModel restModel = mockUtils.createGlobalRestModel();

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

    @Override
    public MockUtils<?, GlobalSchedulingConfigRestModel, ?, GlobalSchedulingConfigEntity> getMockUtil() {
        return mockUtils;
    }

}
