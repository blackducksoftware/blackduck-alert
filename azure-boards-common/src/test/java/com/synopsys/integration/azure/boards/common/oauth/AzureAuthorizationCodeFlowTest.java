package com.synopsys.integration.azure.boards.common.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;

public class AzureAuthorizationCodeFlowTest {
    private final Credential.AccessMethod method = BearerToken.authorizationHeaderAccessMethod();
    private final HttpTransport httpTransport = new ApacheHttpTransport();
    private final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
    private final GenericUrl genericUrl = new GenericUrl("http://tokenServerUrl");
    private final HttpExecuteInterceptor clientAuthentication = Mockito.mock(HttpExecuteInterceptor.class);
    private final String clientId = "clientId";
    private final String authorizationServerEncodedUrl = "http://authorizationServer";
    private final String clientSecret = "Secret";
    private final String redirectUri = "http://redirect";
    private final String authorizationCode = "authorizationCode";

    @Test
    public void newTokenRequestTest() {
        AzureAuthorizationCodeFlow azureAuthorizationCodeFlow = new AzureAuthorizationCodeFlow(
            method,
            httpTransport,
            jsonFactory,
            genericUrl,
            clientAuthentication,
            clientId,
            authorizationServerEncodedUrl,
            clientSecret,
            redirectUri
        );

        AuthorizationCodeTokenRequest tokenRequest = azureAuthorizationCodeFlow.newTokenRequest(authorizationCode);

        assertEquals(authorizationCode, tokenRequest.getCode());
        assertEquals(AzureOAuthConstants.DEFAULT_GRANT_TYPE, tokenRequest.getGrantType());
        assertEquals(redirectUri, tokenRequest.getRedirectUri());
        assertEquals("", tokenRequest.getScopes());
        assertEquals(authorizationCode, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_ASSERTION));
        assertEquals(AzureOAuthConstants.DEFAULT_CLIENT_ASSERTION_TYPE, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE));
        assertEquals(clientSecret, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION));
        assertEquals(redirectUri, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_REDIRECT_URI));
    }

    @Test
    public void getClientSecretAndRedirectUriTest() {
        AzureAuthorizationCodeFlow azureAuthorizationCodeFlow = createAzureAuthorizationCodeFlow();

        assertEquals(clientSecret, azureAuthorizationCodeFlow.getClientSecret());
        assertEquals(redirectUri, azureAuthorizationCodeFlow.getRedirectUri());
    }

    @Test
    public void builderTest() {
        AzureAuthorizationCodeFlow.Builder builder = new AzureAuthorizationCodeFlow.Builder(
            method,
            httpTransport,
            jsonFactory,
            genericUrl,
            clientAuthentication,
            clientId,
            authorizationServerEncodedUrl,
            null,
            null
        );

        assertNull(builder.getClientSecret());
        assertNull(builder.getRedirectUri());
        builder.setClientSecret(clientSecret);
        builder.setRedirectUri(redirectUri);
        assertEquals(clientSecret, builder.getClientSecret());
        assertEquals(redirectUri, builder.getRedirectUri());

        AuthorizationCodeFlow azureAuthorizationCodeFlow = builder.build();

        AuthorizationCodeTokenRequest tokenRequest = azureAuthorizationCodeFlow.newTokenRequest(authorizationCode);
        testAuthorizationCodeTokenRequest(tokenRequest);
    }

    private AzureAuthorizationCodeFlow createAzureAuthorizationCodeFlow() {
        return new AzureAuthorizationCodeFlow(
            method,
            httpTransport,
            jsonFactory,
            genericUrl,
            clientAuthentication,
            clientId,
            authorizationServerEncodedUrl,
            clientSecret,
            redirectUri
        );
    }

    private void testAuthorizationCodeTokenRequest(AuthorizationCodeTokenRequest tokenRequest) {
        assertEquals(authorizationCode, tokenRequest.getCode());
        assertEquals(AzureOAuthConstants.DEFAULT_GRANT_TYPE, tokenRequest.getGrantType());
        assertEquals(redirectUri, tokenRequest.getRedirectUri());
        assertEquals("", tokenRequest.getScopes());
        assertEquals(authorizationCode, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_ASSERTION));
        assertEquals(AzureOAuthConstants.DEFAULT_CLIENT_ASSERTION_TYPE, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE));
        assertEquals(clientSecret, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_CLIENT_ASSERTION));
        assertEquals(redirectUri, tokenRequest.get(AzureOAuthConstants.REQUEST_BODY_FIELD_REDIRECT_URI));
    }
}
