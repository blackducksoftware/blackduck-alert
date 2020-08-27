/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.oauth;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;

public class AzureCredential extends Credential {
    private String clientSecret;
    private String redirectUri;
    private String cachedRefreshToken;

    public AzureCredential(AccessMethod method) {
        super(method);
    }

    public AzureCredential(Builder builder) {
        super(builder);
        this.clientSecret = builder.getClientSecret();
        this.redirectUri = builder.getRedirectUri();
        this.cachedRefreshToken = builder.getCachedRefreshToken();
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCachedRefreshToken() {
        return cachedRefreshToken;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    protected TokenResponse executeRefreshToken() throws IOException {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            if (cachedRefreshToken == null) {
                return null;
            }
            refreshToken = cachedRefreshToken;
        }

        RefreshTokenRequest request = new RefreshTokenRequest(getTransport(), getJsonFactory(), new GenericUrl(getTokenServerEncodedUrl()),
            refreshToken);
        request.setClientAuthentication(getClientAuthentication());
        request.setRequestInitializer(getRequestInitializer());
        request.setResponseClass(AzureTokenResponse.class);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_ASSERTION, refreshToken);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE, AzureOAuthConstants.DEFAULT_CLIENT_ASSERTION_TYPE);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION, clientSecret);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_REDIRECT_URI, redirectUri);
        return request.execute();
    }

    public static class Builder extends Credential.Builder {
        private String clientSecret;
        private String redirectUri;
        private String cachedRefreshToken;

        public Builder(AccessMethod method) {
            super(method);
        }

        public Builder copyFromExisting(Credential credential) {
            this.setCachedRefreshToken(credential.getRefreshToken())
                .setClientAuthentication(credential.getClientAuthentication())
                .setClock(credential.getClock())
                .setJsonFactory(credential.getJsonFactory())
                .setRefreshListeners(credential.getRefreshListeners())
                .setRequestInitializer(credential.getRequestInitializer())
                .setTokenServerEncodedUrl(credential.getTokenServerEncodedUrl())
                .setTransport(credential.getTransport());
            return this;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public String getCachedRefreshToken() {
            return cachedRefreshToken;
        }

        public Builder setCachedRefreshToken(String cachedRefreshToken) {
            this.cachedRefreshToken = cachedRefreshToken;
            return this;
        }

        @Override
        public Credential build() {
            return new AzureCredential(this);
        }
    }
}
