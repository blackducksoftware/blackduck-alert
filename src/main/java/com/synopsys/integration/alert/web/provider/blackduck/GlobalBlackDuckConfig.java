/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.provider.blackduck;

import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.web.model.Config;

public class GlobalBlackDuckConfig extends Config {
    private String blackDuckUrl;
    private String blackDuckTimeout;

    @SensitiveField
    private String blackDuckApiKey;
    private boolean blackDuckApiKeyIsSet;

    private String blackDuckProxyHost;
    private String blackDuckProxyPort;
    private String blackDuckProxyUsername;

    @SensitiveField
    private String blackDuckProxyPassword;
    private boolean blackDuckProxyPasswordIsSet;

    private String blackDuckAlwaysTrustCertificate;

    public GlobalBlackDuckConfig() {
    }

    public GlobalBlackDuckConfig(final String id, final String blackDuckUrl, final String blackDuckTimeout, final String blackDuckApiKey, final boolean blackDuckApiKeyIsSet, final String blackDuckProxyHost, final String blackDuckProxyPort,
            final String blackDuckProxyUsername,
            final String blackDuckProxyPassword, final boolean blackDuckProxyPasswordIsSet, final String blackDuckAlwaysTrustCertificate) {
        super(id);
        this.blackDuckUrl = blackDuckUrl;
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckApiKey = blackDuckApiKey;
        this.blackDuckApiKeyIsSet = blackDuckApiKeyIsSet;
        this.blackDuckProxyHost = blackDuckProxyHost;
        this.blackDuckProxyPort = blackDuckProxyPort;
        this.blackDuckProxyUsername = blackDuckProxyUsername;
        this.blackDuckProxyPassword = blackDuckProxyPassword;
        this.blackDuckProxyPasswordIsSet = blackDuckProxyPasswordIsSet;
        this.blackDuckAlwaysTrustCertificate = blackDuckAlwaysTrustCertificate;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
    }

    public String getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public void setBlackDuckTimeout(final String blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    public boolean isBlackDuckApiKeyIsSet() {
        return blackDuckApiKeyIsSet;
    }

    public void setBlackDuckApiKeyIsSet(final boolean blackDuckApiKeyIsSet) {
        this.blackDuckApiKeyIsSet = blackDuckApiKeyIsSet;
    }

    public String getBlackDuckProxyHost() {
        return blackDuckProxyHost;
    }

    public void setBlackDuckProxyHost(final String blackDuckProxyHost) {
        this.blackDuckProxyHost = blackDuckProxyHost;
    }

    public String getBlackDuckProxyPort() {
        return blackDuckProxyPort;
    }

    public void setBlackDuckProxyPort(final String blackDuckProxyPort) {
        this.blackDuckProxyPort = blackDuckProxyPort;
    }

    public String getBlackDuckProxyUsername() {
        return blackDuckProxyUsername;
    }

    public void setBlackDuckProxyUsername(final String blackDuckProxyUsername) {
        this.blackDuckProxyUsername = blackDuckProxyUsername;
    }

    public String getBlackDuckProxyPassword() {
        return blackDuckProxyPassword;
    }

    public void setBlackDuckProxyPassword(final String blackDuckProxyPassword) {
        this.blackDuckProxyPassword = blackDuckProxyPassword;
    }

    public boolean isBlackDuckProxyPasswordIsSet() {
        return blackDuckProxyPasswordIsSet;
    }

    public void setBlackDuckProxyPasswordIsSet(final boolean blackDuckProxyPasswordIsSet) {
        this.blackDuckProxyPasswordIsSet = blackDuckProxyPasswordIsSet;
    }

    public String getBlackDuckAlwaysTrustCertificate() {
        return blackDuckAlwaysTrustCertificate;
    }

    public void setBlackDuckAlwaysTrustCertificate(final String blackDuckAlwaysTrustCertificate) {
        this.blackDuckAlwaysTrustCertificate = blackDuckAlwaysTrustCertificate;
    }

    public void setBlackDuckApiKey(final String blackDuckApiKey) {
        this.blackDuckApiKey = blackDuckApiKey;
    }

}
