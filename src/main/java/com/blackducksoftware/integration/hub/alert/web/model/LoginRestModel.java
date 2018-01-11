/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveField;

public class LoginRestModel extends ConfigRestModel {
    private String hubUrl;
    private String hubTimeout;
    private String hubUsername;
    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;
    private String hubAlwaysTrustCertificate;

    @SensitiveField
    private String hubPassword;

    @SensitiveField
    private String hubProxyPassword;

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

}
