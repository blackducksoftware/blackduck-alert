/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.azure.boards.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonString;
import com.google.api.client.util.Key;

public class AzureTokenResponse extends TokenResponse {

    // Azure OAuth returns the expires_in as a string not a numeric value.
    // need to override and create a customer TokenResponse to use the JSonString annotation.
    @Key("expires_in")
    @JsonString
    private Long expiresInSeconds;

    public AzureTokenResponse() {
        super();
    }

    @Override
    public String getAccessToken() {
        return super.getAccessToken();
    }

    @Override
    public AzureTokenResponse setAccessToken(String accessToken) {
        super.setAccessToken(accessToken);
        return this;
    }

    @Override
    public String getTokenType() {
        return super.getTokenType();
    }

    @Override
    public AzureTokenResponse setTokenType(String tokenType) {
        super.setTokenType(tokenType);
        return this;
    }

    @Override
    public Long getExpiresInSeconds() {
        return this.expiresInSeconds;
    }

    @Override
    public AzureTokenResponse setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
        return this;
    }

    @Override
    public String getRefreshToken() {
        return super.getRefreshToken();
    }

    @Override
    public AzureTokenResponse setRefreshToken(String refreshToken) {
        super.setRefreshToken(refreshToken);
        return this;
    }

    @Override
    public String getScope() {
        return super.getScope();
    }

    @Override
    public AzureTokenResponse setScope(String scope) {
        super.setScope(scope);
        return this;
    }

    @Override
    public AzureTokenResponse set(String fieldName, Object value) {
        super.set(fieldName, value);
        return this;
    }

    @Override
    public AzureTokenResponse clone() {
        return (AzureTokenResponse) super.clone();
    }
}
