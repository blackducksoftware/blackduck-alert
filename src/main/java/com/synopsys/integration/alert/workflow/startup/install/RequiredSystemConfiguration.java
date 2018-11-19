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
package com.synopsys.integration.alert.workflow.startup.install;

import com.synopsys.integration.util.Stringable;

public class RequiredSystemConfiguration extends Stringable {
    private final String blackDuckProviderUrl;
    private final Integer blackDuckConnectionTimeout;
    private final String blackDuckApiToken;
    private final String globalEncryptionPassword;
    private final String globalEncryptionSalt;
    private final boolean globalEncryptionPasswordSet;
    private final boolean globlaEncryptionSaltSet;

    private final String proxyHost;
    private final String proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    public RequiredSystemConfiguration(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken,
        final boolean isGlobalEncryptionPasswordSet,
        final boolean isGlobalEncryptionSaltSet,
        final String proxyHost, final String proxyPort, final String proxyUsername, final String proxyPassword) {
        this(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, null, isGlobalEncryptionPasswordSet, null, isGlobalEncryptionSaltSet, proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    public RequiredSystemConfiguration(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken,
        final String globalEncryptionPassword, final boolean isGlobalEncryptionPasswordSet,
        final String globalEncryptionSalt, final boolean isGlobalEncryptionSaltSet,
        final String proxyHost, final String proxyPort, final String proxyUsername, final String proxyPassword) {
        this.blackDuckProviderUrl = blackDuckProviderUrl;
        this.blackDuckConnectionTimeout = blackDuckConnectionTimeout;
        this.blackDuckApiToken = blackDuckApiToken;
        this.globalEncryptionPassword = globalEncryptionPassword;
        this.globalEncryptionPasswordSet = isGlobalEncryptionPasswordSet;
        this.globalEncryptionSalt = globalEncryptionSalt;
        this.globlaEncryptionSaltSet = isGlobalEncryptionSaltSet;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
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

    public String getGlobalEncryptionPassword() {
        return globalEncryptionPassword;
    }

    public boolean isGlobalEncryptionPasswordSet() {
        return globalEncryptionPasswordSet;
    }

    public String getGlobalEncryptionSalt() {
        return globalEncryptionSalt;
    }

    public boolean isGloblaEncryptionSaltSet() {
        return globlaEncryptionSaltSet;
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
}
