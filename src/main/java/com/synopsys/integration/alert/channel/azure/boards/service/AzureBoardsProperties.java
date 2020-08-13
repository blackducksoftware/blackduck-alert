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

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerServiceConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;

public class AzureBoardsProperties implements IssueTrackerServiceConfig {
    private static final String DEFAULT_AZURE_OAUTH_USER_ID = "azure_default_user";
    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final String organizationName;
    private final String clientId;
    private final String oauthUserId;
    private final List<String> scopes;

    public static AzureBoardsProperties fromFieldAccessor(AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, FieldAccessor fieldAccessor) {
        String organizationName = fieldAccessor.getStringOrNull(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
        String clientId = fieldAccessor.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_ID);
        String oAuthUserEmail = fieldAccessor.getString(AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL).orElse(DEFAULT_AZURE_OAUTH_USER_ID);
        //TODO fix the app scope to have project read.  Need to change the scope of the registered application.
        List<String> defaultScopes = List.of(AzureOAuthScopes.PROJECTS_WRITE.getScope(), AzureOAuthScopes.WORK_FULL.getScope());
        return new AzureBoardsProperties(credentialDataStoreFactory, organizationName, clientId, oAuthUserEmail, defaultScopes);
    }

    public AzureBoardsProperties(AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, String organizationName, String clientId, String oauthUserId, List<String> scopes) {
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.organizationName = organizationName;
        this.clientId = clientId;
        this.oauthUserId = oauthUserId;
        this.scopes = scopes;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOauthUserId() {
        return oauthUserId;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public AzureHttpService createAzureHttpService(Proxy proxy, Gson gson) throws AlertException {
        NetHttpTransport httpTransport = createHttpTransport(proxy);
        try {
            AuthorizationCodeFlow oAuthFlow = createOAuthFlow(httpTransport);
            Credential oAuthCredential = getExistingOAuthCredential(oAuthFlow)
                                             .orElseThrow(() -> new AlertException(String.format("No existing Azure OAuth credential for the user '%s'", oauthUserId)));
            return AzureHttpServiceFactory.withCredential(httpTransport, oAuthCredential, gson);
        } catch (IOException e) {
            throw new AlertException("Cannot read OAuth credentials", e);
        }
    }

    public AuthorizationCodeFlow createOAuthFlow(NetHttpTransport httpTransport) throws IOException {
        return createOAuthFlowBuilder(httpTransport)
                   .setCredentialDataStore(StoredCredential.getDefaultDataStore(credentialDataStoreFactory))
                   .addRefreshListener(new DataStoreCredentialRefreshListener(oauthUserId, credentialDataStoreFactory))
                   .build();
    }

    public AuthorizationCodeFlow.Builder createOAuthFlowBuilder(NetHttpTransport httpTransport) {
        return createOAuthFlowBuilder(httpTransport, BearerToken.authorizationHeaderAccessMethod());
    }

    public AuthorizationCodeFlow.Builder createOAuthFlowBuilder(NetHttpTransport httpTransport, Credential.AccessMethod authorizationAccessMethod) {
        return new AuthorizationCodeFlow.Builder(
            authorizationAccessMethod,
            httpTransport,
            JacksonFactory.getDefaultInstance(),
            new GenericUrl(AzureHttpServiceFactory.DEFAULT_TOKEN_URL),
            null,
            clientId,
            encode(AzureHttpServiceFactory.DEFAULT_AUTHORIZATION_URL)
        ).setScopes(getScopes());
    }

    public NetHttpTransport createHttpTransport(Proxy proxy) {
        return new NetHttpTransport.Builder()
                   .setProxy(proxy)
                   .build();
    }

    public Optional<Credential> getExistingOAuthCredential(AuthorizationCodeFlow authorizationCodeFlow) throws IOException {
        Credential storedCredential = authorizationCodeFlow.loadCredential(oauthUserId);
        return Optional.ofNullable(storedCredential);
    }

    private String encode(String str) {
        byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
        return new String(encodedBytes);
    }

}
