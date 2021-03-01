/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.http;

import java.io.IOException;
import java.net.Proxy;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
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

    private String sanitizeUrl(String url) {
        url = StringUtils.trim(url);
        if (StringUtils.endsWith(url, "/")) {
            return StringUtils.chomp(url);
        }
        return url;
    }

}
