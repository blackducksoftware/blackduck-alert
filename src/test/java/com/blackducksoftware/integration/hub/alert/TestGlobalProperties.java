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
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;

// TODO Are GlobalProperties something we may want to create a mock object with?
public class TestGlobalProperties extends GlobalProperties {
    private Integer hubTimeout;
    private String hubUsername;
    private String hubPassword;
    private String accumulatorCron;
    private String dailyDigestCron;

    public TestGlobalProperties() {
        this(Mockito.mock(GlobalHubRepository.class), Mockito.mock(GlobalSchedulingRepository.class));
    }

    public TestGlobalProperties(final GlobalHubRepository globalRepository, final GlobalSchedulingRepository globalSchedulingRepository) {
        this(globalRepository, globalSchedulingRepository, 400, "hubUsername", "hubPassword", "1 1 1 1 1 1", "2 2 2 2 2 2");
    }

    public TestGlobalProperties(final GlobalHubRepository globalRepository, final GlobalSchedulingRepository globalSchedulingRepository, final Integer hubTimeout, final String hubUsername, final String hubPassword,
            final String accumulatorCron, final String dailyDigestCron) {
        super(globalRepository, globalSchedulingRepository);
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
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
    public String getHubUsername() {
        return hubUsername;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    @Override
    public String getHubPassword() {
        return hubPassword;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
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
