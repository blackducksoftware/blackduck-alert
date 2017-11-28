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
package com.blackducksoftware.integration.hub.alert.web.model;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class LoginRestModel extends ConfigRestModel {
    private static final long serialVersionUID = 9172607945030111585L;

    private String hubUrl;
    private String hubTimeout;
    private String hubUsername;
    private String hubPassword;
    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;
    private String hubProxyPassword;
    private String hubAlwaysTrustCertificate;

    public LoginRestModel() {
    }

    public LoginRestModel(final String id, final String hubUrl, final String hubTimeout, final String hubUsername, final String hubPassword, final String hubProxyHost, final String hubProxyPort, final String hubProxyUsername,
            final String hubProxyPassword, final String hubAlwaysTrustCertificate) {
        super(id);
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubTimeout() {
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

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        reflectionToStringBuilder.setExcludeFieldNames("hubPassword");
        reflectionToStringBuilder.setExcludeFieldNames("hubProxyPassword");
        return reflectionToStringBuilder.toString();
    }

}
