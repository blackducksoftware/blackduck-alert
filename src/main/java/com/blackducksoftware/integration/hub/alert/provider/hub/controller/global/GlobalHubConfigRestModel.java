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
package com.blackducksoftware.integration.hub.alert.provider.hub.controller.global;

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class GlobalHubConfigRestModel extends ConfigRestModel {
    private String hubUrl;
    private String hubTimeout;

    @SensitiveField
    private String hubApiKey;
    private boolean hubApiKeyIsSet;

    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;

    @SensitiveField
    private String hubProxyPassword;
    private boolean hubProxyPasswordIsSet;

    private String hubAlwaysTrustCertificate;

    public GlobalHubConfigRestModel() {
    }

    public GlobalHubConfigRestModel(final String id, final String hubUrl, final String hubTimeout, final String hubApiKey, final boolean hubApiKeyIsSet, final String hubProxyHost, final String hubProxyPort, final String hubProxyUsername,
            final String hubProxyPassword, final boolean hubProxyPasswordIsSet, final String hubAlwaysTrustCertificate) {
        super(id);
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubApiKey = hubApiKey;
        this.hubApiKeyIsSet = hubApiKeyIsSet;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubProxyPasswordIsSet = hubProxyPasswordIsSet;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final String hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public String getHubApiKey() {
        return hubApiKey;
    }

    public boolean isHubApiKeyIsSet() {
        return hubApiKeyIsSet;
    }

    public void setHubApiKeyIsSet(final boolean hubApiKeyIsSet) {
        this.hubApiKeyIsSet = hubApiKeyIsSet;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public boolean isHubProxyPasswordIsSet() {
        return hubProxyPasswordIsSet;
    }

    public void setHubProxyPasswordIsSet(final boolean hubProxyPasswordIsSet) {
        this.hubProxyPasswordIsSet = hubProxyPasswordIsSet;
    }

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public void setHubAlwaysTrustCertificate(final String hubAlwaysTrustCertificate) {
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

}
