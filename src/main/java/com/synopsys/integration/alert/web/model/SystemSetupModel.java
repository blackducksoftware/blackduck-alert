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
    private boolean blackDuckApiTokenSet;
    @SensitiveField
    private String globalEncryptionPassword;
    private boolean globalEncryptionPasswordSet;
    @SensitiveField
    private String globalEncryptionSalt;
    private boolean globalEncryptionSaltSet;

    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    @SensitiveField
    private String proxyPassword;
    private boolean proxyPasswordSet;

    public SystemSetupModel() {

    }

    public SystemSetupModel(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken, final boolean blackDuckApiTokenSet,
        final String globalEncryptionPassword, final boolean globalEncryptionPasswordSet, final String globalEncryptionSalt, final boolean globalEncryptionSaltSet,
        final String proxyHost, final String proxyPort, final String proxyUsername, final String proxyPassword, final boolean proxyPasswordSet) {
        this.blackDuckProviderUrl = blackDuckProviderUrl;
        this.blackDuckConnectionTimeout = blackDuckConnectionTimeout;
        this.blackDuckApiToken = blackDuckApiToken;
        this.blackDuckApiTokenSet = blackDuckApiTokenSet;
        this.globalEncryptionPassword = globalEncryptionPassword;
        this.globalEncryptionPasswordSet = globalEncryptionPasswordSet;
        this.globalEncryptionSalt = globalEncryptionSalt;
        this.globalEncryptionSaltSet = globalEncryptionSaltSet;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.proxyPasswordSet = proxyPasswordSet;
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

    public boolean isBlackDuckApiTokenSet() {
        return blackDuckApiTokenSet;
    }

    public String getGlobalEncryptionPassword() {
        return globalEncryptionPassword;
    }

    public boolean isGlobalEncryptionPasswordSet() {
        return globalEncryptionPasswordSet;
    }

    public String getGlobalEncryptionSalt() {
        return globalEncryptionSalt;
    }

    public boolean isGlobalEncryptionSaltSet() {
        return globalEncryptionSaltSet;
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

    public boolean isProxyPasswordSet() {
        return proxyPasswordSet;
    }
}
