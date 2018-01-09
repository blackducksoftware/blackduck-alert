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
package com.blackducksoftware.integration.hub.alert;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepositoryWrapper;

// TODO Are GlobalProperties something we may want to create a mock object with?
public class TestGlobalProperties extends GlobalProperties {
    private Integer hubTimeout;
    private String hubApiKey;
    private String accumulatorCron;
    private String dailyDigestCron;

    public TestGlobalProperties() {
        this(Mockito.mock(GlobalHubRepositoryWrapper.class), Mockito.mock(GlobalSchedulingRepositoryWrapper.class));
    }

    public TestGlobalProperties(final GlobalHubRepositoryWrapper globalHubRepositoryWrapper, final GlobalSchedulingRepositoryWrapper globalSchedulingRepository) {
        this(globalHubRepositoryWrapper, globalSchedulingRepository, 400, "1 1 1 1 1 1", "2 2 2 2 2 2");
    }

    public TestGlobalProperties(final GlobalHubRepositoryWrapper globalHubRepositoryWrapper, final GlobalSchedulingRepositoryWrapper globalSchedulingRepository, final Integer hubTimeout, final String accumulatorCron,
            final String dailyDigestCron) {
        super(globalHubRepositoryWrapper, globalSchedulingRepository);
        this.hubTimeout = hubTimeout;
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
    }

    @Override
    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final Integer hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    @Override
    public String getHubApiKey() {
        return hubApiKey;
    }

    public void setHubApiKey(final String hubApiKey) {
        this.hubApiKey = hubApiKey;
    }

    @Override
    public String getAccumulatorCron() {
        return accumulatorCron;
    }

    public void setAccumulatorCron(final String accumulatorCron) {
        this.accumulatorCron = accumulatorCron;
    }

    @Override
    public String getDailyDigestCron() {
        return dailyDigestCron;
    }

    public void setDailyDigestCron(final String dailyDigestCron) {
        this.dailyDigestCron = dailyDigestCron;
    }

}
