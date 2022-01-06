/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.oauth;

import java.io.IOException;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Preconditions;

public class AzureAuthorizationCodeFlow extends AuthorizationCodeFlow {
    private final String clientSecret;
    private final String redirectUri;

    public AzureAuthorizationCodeFlow(Credential.AccessMethod method, HttpTransport transport, JsonFactory jsonFactory,
        GenericUrl tokenServerUrl, HttpExecuteInterceptor clientAuthentication, String clientId, String authorizationServerEncodedUrl, String clientSecret, String redirectUri) {
        super(method, transport, jsonFactory, tokenServerUrl, clientAuthentication, clientId, authorizationServerEncodedUrl);
        this.clientSecret = Preconditions.checkNotNull(clientSecret);
        this.redirectUri = Preconditions.checkNotNull(redirectUri);
    }

    protected AzureAuthorizationCodeFlow(Builder builder) {
        super(builder);
        this.clientSecret = Preconditions.checkNotNull(builder.clientSecret);
        this.redirectUri = Preconditions.checkNotNull(builder.redirectUri);
    }

    @Override
    public AuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
        AuthorizationCodeTokenRequest request = super.newTokenRequest(authorizationCode);
        request.setGrantType(AzureOAuthConstants.DEFAULT_GRANT_TYPE);
        request.setResponseClass(AzureTokenResponse.class);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_ASSERTION, authorizationCode);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE, AzureOAuthConstants.DEFAULT_CLIENT_ASSERTION_TYPE);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION, clientSecret);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_REDIRECT_URI, redirectUri);
        return request;
    }

    @Override
    public Credential loadCredential(String userId) throws IOException {
        Credential credential = super.loadCredential(userId);
        if (null == credential) {
            return null;
        }
        AzureCredential.Builder credentialBuilder = new AzureCredential.Builder(credential.getMethod());
        credentialBuilder.copyFromExisting(credential);
        credentialBuilder.setRedirectUri(redirectUri);
        credentialBuilder.setClientSecret(clientSecret);

        return credentialBuilder.build();
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public static class Builder extends AuthorizationCodeFlow.Builder {
        private String clientSecret;
        private String redirectUri;

        public Builder(Credential.AccessMethod method, HttpTransport transport, JsonFactory jsonFactory, GenericUrl tokenServerUrl, HttpExecuteInterceptor clientAuthentication, String clientId,
            String authorizationServerEncodedUrl, String clientSecret, String redirectUri) {
            super(method, transport, jsonFactory, tokenServerUrl, clientAuthentication, clientId, authorizationServerEncodedUrl);
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
        }

        @Override
        public AuthorizationCodeFlow build() {
            return new AzureAuthorizationCodeFlow(this);
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
    }
}
