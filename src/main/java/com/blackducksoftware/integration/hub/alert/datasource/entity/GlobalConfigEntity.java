/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Entity
@Table(name = "global_config")
public class GlobalConfigEntity extends DatabaseEntity {
    private static final long serialVersionUID = 9172607945030111585L;

    @Column(name = "hub_url")
    private String hubUrl;

    @Column(name = "hub_timeout")
    private Integer hubTimeout;

    @Column(name = "hub_username")
    private String hubUsername;

    @Column(name = "hub_password")
    private String hubPassword;

    @Column(name = "hub_proxy_host")
    private String hubProxyHost;

    @Column(name = "hub_proxy_port")
    private String hubProxyPort;

    @Column(name = "hub_proxy_username")
    private String hubProxyUsername;

    @Column(name = "hub_proxy_password")
    private String hubProxyPassword;

    @Column(name = "hub_always_trust_cert")
    private Boolean hubAlwaysTrustCertificate;

    @Column(name = "alert_accumulator_cron")
    private String accumulatorCron;

    @Column(name = "alert_digest_daily_cron")
    private String dailyDigestCron;

    public GlobalConfigEntity() {
    }

    public GlobalConfigEntity(final String hubUrl, final Integer hubTimeout, final String hubUsername, final String hubPassword, final String hubProxyHost, final String hubProxyPort, final String hubProxyUsername,
            final String hubProxyPassword, final Boolean hubAlwaysTrustCertificate, final String accumulatorCron, final String dailyDigestCron) {
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
        this.accumulatorCron = accumulatorCron;
        this.dailyDigestCron = dailyDigestCron;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public Boolean getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public String getAccumulatorCron() {
        return accumulatorCron;
    }

    public String getDailyDigestCron() {
        return dailyDigestCron;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        reflectionToStringBuilder.setExcludeFieldNames("hubPassword");
        reflectionToStringBuilder.setExcludeFieldNames("hubProxyPassword");
        return reflectionToStringBuilder.toString();
    }

}
