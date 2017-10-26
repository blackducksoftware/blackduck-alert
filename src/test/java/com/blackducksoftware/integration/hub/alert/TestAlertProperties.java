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

import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalRepository;

public class TestAlertProperties extends GlobalProperties {

    private String hubUrl;
    private Integer hubTimeout;
    private String hubUsername;
    private String hubPassword;
    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;
    private String hubProxyPassword;
    private Boolean hubAlwaysTrustCertificate;
    private String accumulatorCron;
    private String dailyDigestCron;
    private String realTimeDigestCron;

    public TestAlertProperties(final GlobalRepository globalRepository) {
        super(globalRepository);
    }

    @Override
    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
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
    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    @Override
    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    @Override
    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    @Override
    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    @Override
    public Boolean getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public void setHubAlwaysTrustCertificate(final Boolean hubAlwaysTrustCertificate) {
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
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

    @Override
    public String getRealTimeDigestCron() {
        return realTimeDigestCron;
    }

    public void setRealTimeDigestCron(final String realTimeDigestCron) {
        this.realTimeDigestCron = realTimeDigestCron;
    }

}
