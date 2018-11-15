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
package com.synopsys.integration.alert.web.model;

import com.synopsys.integration.alert.common.annotation.SensitiveField;

public class SystemSetupModel extends MaskedModel {
    private String blackDuckProviderUrl;
    private Integer blackDuckConnectionTimeout;
    @SensitiveField
    private String blackDuckApiToken;
    private boolean blackDuckApiTokenIsSet;
    @SensitiveField
    private String globalEncryptionPassword;
    private boolean globalEncryptionPasswordIsSet;
    @SensitiveField
    private String globalEncryptionSalt;
    private boolean globalEncryptionSaltIsSet;

    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    @SensitiveField
    private String proxyPassword;
    private boolean proxyPasswordIsSet;

    public SystemSetupModel() {

    }

    public SystemSetupModel(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken, final boolean blackDuckApiTokenIsSet,
        final String globalEncryptionPassword, final boolean globalEncryptionPasswordIsSet, final String globalEncryptionSalt, final boolean globalEncryptionSaltIsSet,
        final String proxyHost, final String proxyPort, final String proxyUsername, final String proxyPassword, final boolean proxyPasswordIsSet) {
        this.blackDuckProviderUrl = blackDuckProviderUrl;
        this.blackDuckConnectionTimeout = blackDuckConnectionTimeout;
        this.blackDuckApiToken = blackDuckApiToken;
        this.blackDuckApiTokenIsSet = blackDuckApiTokenIsSet;
        this.globalEncryptionPassword = globalEncryptionPassword;
        this.globalEncryptionPasswordIsSet = globalEncryptionPasswordIsSet;
        this.globalEncryptionSalt = globalEncryptionSalt;
        this.globalEncryptionSaltIsSet = globalEncryptionSaltIsSet;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.proxyPasswordIsSet = proxyPasswordIsSet;
    }

    public String getBlackDuckProviderUrl() {
        return blackDuckProviderUrl;
    }

    public Integer getBlackDuckConnectionTimeout() {
        return blackDuckConnectionTimeout;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public boolean isBlackDuckApiTokenIsSet() {
        return blackDuckApiTokenIsSet;
    }

    public String getGlobalEncryptionPassword() {
        return globalEncryptionPassword;
    }

    public boolean isGlobalEncryptionPasswordIsSet() {
        return globalEncryptionPasswordIsSet;
    }

    public String getGlobalEncryptionSalt() {
        return globalEncryptionSalt;
    }

    public boolean isGlobalEncryptionSaltIsSet() {
        return globalEncryptionSaltIsSet;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public boolean isProxyPasswordIsSet() {
        return proxyPasswordIsSet;
    }
}
