package com.synopsys.integration.azure.boards.common.oauth;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;

public class AzureCredential extends Credential {
    private String clientSecret;
    private String redirectUri;

    public AzureCredential(AccessMethod method) {
        super(method);
    }

    public AzureCredential(Builder builder) {
        super(builder);
        this.clientSecret = builder.getClientSecret();
        this.redirectUri = builder.getRedirectUri();
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    protected TokenResponse executeRefreshToken() throws IOException {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            return null;
        }
        RefreshTokenRequest request = new RefreshTokenRequest(getTransport(), getJsonFactory(), new GenericUrl(getTokenServerEncodedUrl()),
            refreshToken);
        request.setClientAuthentication(getClientAuthentication());
        request.setRequestInitializer(getRequestInitializer());
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_ASSERTION, refreshToken);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE, AzureOAuthConstants.DEFAULT_CLIENT_ASSERTION_TYPE);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION, clientSecret);
        request.put(AzureOAuthConstants.REQUEST_BODY_FIELD_REDIRECT_URI, redirectUri);
        return request.execute();
    }

    public static class Builder extends Credential.Builder {
        private String clientSecret;
        private String redirectUri;

        public Builder(AccessMethod method) {
            super(method);
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

        @Override
        public Credential build() {
            return new AzureCredential(this);
        }
    }
}
