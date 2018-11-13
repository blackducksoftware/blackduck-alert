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
package com.synopsys.integration.alert.install;

import com.synopsys.integration.util.Stringable;

public class RequiredSystemConfiguration extends Stringable {
    private final String blackDuckProviderUrl;
    private final Integer blackDuckConnectionTimeout;
    private final String blackDuckApiToken;
    private final String globalEncryptionPassword;
    private final String globalEncryptionSalt;

    public RequiredSystemConfiguration(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken, final String globalEncryptionPassword, final String globalEncryptionSalt) {
        this.blackDuckProviderUrl = blackDuckProviderUrl;
        this.blackDuckConnectionTimeout = blackDuckConnectionTimeout;
        this.blackDuckApiToken = blackDuckApiToken;
        this.globalEncryptionPassword = globalEncryptionPassword;
        this.globalEncryptionSalt = globalEncryptionSalt;
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

    public String getGlobalEncryptionSalt() {
        return globalEncryptionSalt;
    }
}
