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
package com.synopsys.integration.azure.boards.common.http;

import java.io.IOException;
import java.net.Proxy;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.DataStoreFactory;
import com.synopsys.integration.exception.IntegrationException;

public class AzureBoardsOAuthService {
    private final String authorizationServerUrl;
    private final Proxy proxy;
    private final DataStoreFactory dataStoreFactory;

    public AzureBoardsOAuthService(String authorizationServerUrl, Proxy proxy, DataStoreFactory dataStoreFactory) {
        this.authorizationServerUrl = sanitizeUrl(authorizationServerUrl);
        this.proxy = proxy;
        this.dataStoreFactory = dataStoreFactory;
    }

    public Optional<Credential> getExistingOAuthCredential(AuthorizationCodeFlow authorizationCodeFlow, String userId) throws IntegrationException {
        try {
            Credential storedCredential = authorizationCodeFlow.loadCredential(userId);
            return Optional.ofNullable(storedCredential);
        } catch (IOException e) {
            throw new IntegrationException(e);
        }
    }

    public String getAuthorizationUrl(AuthorizationCodeFlow authorizationCodeFlow, String redirectUri) {
        return authorizationCodeFlow.newAuthorizationUrl()
                   .setClientId(authorizationCodeFlow.getClientId())
                   .setRedirectUri(redirectUri)
                   .build();
    }

    public Credential authorizeAndStoreCredential(AuthorizationCodeFlow authorizationCodeFlow, String userId, String authorizationCode) throws IntegrationException {
        try {
            AuthorizationCodeTokenRequest authorizationCodeTokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode);
            TokenResponse tokenResponse = authorizationCodeTokenRequest.execute();
            return authorizationCodeFlow.createAndStoreCredential(tokenResponse, userId);
        } catch (IOException e) {
            throw new IntegrationException(e);
        }
    }

    private AuthorizationCodeFlow initializeFlow(String clientId) throws IOException {
        return new AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(),
            new NetHttpTransport.Builder().setProxy(proxy).build(),
            JacksonFactory.getDefaultInstance(),
            new GenericUrl(),
            null,
            clientId,
            encode(authorizationServerUrl)
        )
                   .setCredentialDataStore(StoredCredential.getDefaultDataStore(dataStoreFactory))
                   .build();
    }

    private String encode(String str) {
        byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
        return new String(encodedBytes);
    }

    private String sanitizeUrl(String url) {
        url = StringUtils.trim(url);
        if (StringUtils.endsWith(url, "/")) {
            return StringUtils.chomp(url);
        }
        return url;
    }

}
