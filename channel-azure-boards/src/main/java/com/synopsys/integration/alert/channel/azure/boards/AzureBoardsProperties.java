/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreator;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.oauth.AzureAuthorizationCodeFlow;
import com.synopsys.integration.azure.boards.common.oauth.AzureOAuthScopes;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class AzureBoardsProperties {
    private static final String DEFAULT_AZURE_OAUTH_USER_ID = "azure_default_user";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final String organizationName;
    private final String clientId;
    private final String clientSecret;
    private final String oauthUserId;
    private final List<String> scopes;
    private final String redirectUri;

    public static AzureBoardsProperties fromFieldAccessor(AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, String redirectUri, FieldUtility fieldUtility) {
        String organizationName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
        String clientId = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_ID);
        String clientSecret = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_SECRET);
        String oAuthUserEmail = fieldUtility.getString(AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL).orElse(DEFAULT_AZURE_OAUTH_USER_ID);
        List<String> defaultScopes = List.of(AzureOAuthScopes.PROJECTS_READ.getScope(), AzureOAuthScopes.WORK_FULL.getScope());
        return new AzureBoardsProperties(credentialDataStoreFactory, organizationName, clientId, clientSecret, oAuthUserEmail, defaultScopes, redirectUri);
    }

    public static AzureBoardsProperties fromGlobalConfig(AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, String redirectUri, ConfigurationModel globalConfiguration) {
        FieldUtility globalFieldUtility = new FieldUtility(globalConfiguration.getCopyOfKeyToFieldMap());
        return fromFieldAccessor(credentialDataStoreFactory, redirectUri, globalFieldUtility);
    }

    public AzureBoardsProperties(AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, String organizationName, String clientId, String clientSecret, String oauthUserId, List<String> scopes, String redirectUri) {
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.organizationName = organizationName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.oauthUserId = oauthUserId;
        this.scopes = scopes;
        this.redirectUri = redirectUri;

        logger.debug("Initializing Azure Boards Properties with values: organizationName=[{}], oAuthUserId=[{}], scopes=[{}], redirectUri=[{}]", organizationName, oauthUserId, StringUtils.join(scopes, ","), redirectUri);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getOauthUserId() {
        return oauthUserId;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void validateProperties() throws AlertConfigurationException {
        if (StringUtils.isBlank(organizationName) || StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new AlertConfigurationException("The global configuration for Azure is missing required information.");
        }
    }

    public AzureHttpService createAzureHttpService(ProxyInfo proxy, Gson gson, String authorizationCode) throws AlertException {
        HttpTransport httpTransport = createHttpTransport(proxy);
        try {
            AuthorizationCodeFlow oAuthFlow = createOAuthFlow(httpTransport);
            Credential oAuthCredential = requestTokens(oAuthFlow, authorizationCode)
                .orElseThrow(() -> new AlertException(String.format("Cannot request Azure OAuth credential associated with '%s'", oauthUserId)));

            AzureHttpRequestCreator httpRequestCreator = AzureHttpRequestCreatorFactory.withCredential(httpTransport, oAuthCredential, gson);
            return new AzureHttpService(gson, httpRequestCreator);
        } catch (IOException e) {
            throw new AlertException("Cannot request OAuth credentials", e);
        }
    }

    public AzureHttpService createAzureHttpService(ProxyInfo proxyInfo, Gson gson) throws AlertException {
        AzureHttpRequestCreator httpRequestCreator = createAzureHttpRequestCreator(proxyInfo, gson);
        return new AzureHttpService(gson, httpRequestCreator);
    }

    public AzureHttpRequestCreator createAzureHttpRequestCreator(ProxyInfo proxyInfo, Gson gson) throws AlertException {
        HttpTransport httpTransport = createHttpTransport(proxyInfo);
        try {
            AuthorizationCodeFlow oAuthFlow = createOAuthFlow(httpTransport);
            Credential oAuthCredential = getExistingOAuthCredential(oAuthFlow)
                .orElseThrow(() -> new AlertException(String.format("No existing Azure OAuth credential associated with '%s'", oauthUserId)));
            return AzureHttpRequestCreatorFactory.withCredential(httpTransport, oAuthCredential, gson);
        } catch (IOException e) {
            throw new AlertException("Cannot read OAuth credentials", e);
        }
    }

    public AuthorizationCodeFlow createOAuthFlow(HttpTransport httpTransport) throws IOException {
        return createOAuthFlowBuilder(httpTransport)
            .setCredentialDataStore(StoredCredential.getDefaultDataStore(credentialDataStoreFactory))
            .addRefreshListener(new DataStoreCredentialRefreshListener(oauthUserId, credentialDataStoreFactory))
            .build();
    }

    public AuthorizationCodeFlow.Builder createOAuthFlowBuilder(HttpTransport httpTransport) {
        return createOAuthFlowBuilder(httpTransport, BearerToken.authorizationHeaderAccessMethod());
    }

    public AuthorizationCodeFlow.Builder createOAuthFlowBuilder(HttpTransport httpTransport, Credential.AccessMethod authorizationAccessMethod) {
        return new AzureAuthorizationCodeFlow.Builder(
            authorizationAccessMethod,
            httpTransport,
            JacksonFactory.getDefaultInstance(),
            new GenericUrl(AzureHttpRequestCreatorFactory.DEFAULT_TOKEN_URL),
            new ClientParametersAuthentication(clientId, clientSecret),
            clientId,
            encode(AzureHttpRequestCreatorFactory.DEFAULT_AUTHORIZATION_URL),
            clientSecret,
            redirectUri
        ).setScopes(getScopes());
    }

    public HttpTransport createHttpTransport(ProxyInfo proxyInfo) {
        // Authenticated proxies aren't supported with the OAuth client library by default.
        // Need to use an Apache Http Client backed transport to support authenticated proxies.
        // Setup the client as the int-rest project does. That is known to setup a client that supports authenticated proxies.
        // https://github.com/googleapis/google-http-java-client/issues/190

        HttpClientBuilder httpClientBuilder = ApacheHttpTransport.newDefaultHttpClientBuilder();
        if (proxyInfo.shouldUseProxy()) {
            String proxyHost = proxyInfo.getHost().orElse(null);
            int proxyPort = proxyInfo.getPort();

            InetSocketAddress proxyAddress = InetSocketAddress.createUnresolved(proxyHost, proxyPort);
            ProxySelector proxySelector = ProxySelector.of(proxyAddress);
            httpClientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(proxySelector));

            httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
            if (proxyInfo.hasAuthenticatedProxySettings()) {
                NTCredentials credentials = new NTCredentials(
                    proxyInfo.getUsername().orElse(null),
                    proxyInfo.getPassword().orElse(null),
                    proxyInfo.getNtlmWorkstation().orElse(null),
                    proxyInfo.getNtlmDomain().orElse(null)
                );
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost().orElse(null), proxyInfo.getPort()), credentials);

                httpClientBuilder.setProxyAuthenticationStrategy(ProxyAuthenticationStrategy.INSTANCE);
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        return new ApacheHttpTransport(httpClientBuilder.build());
    }

    public Optional<Credential> getExistingOAuthCredential(AuthorizationCodeFlow authorizationCodeFlow) throws IOException {
        Credential storedCredential = authorizationCodeFlow.loadCredential(oauthUserId);
        return Optional.ofNullable(storedCredential);
    }

    public boolean hasOAuthCredentials(ProxyInfo proxy) {
        HttpTransport httpTransport = createHttpTransport(proxy);
        try {
            AuthorizationCodeFlow oAuthFlow = createOAuthFlow(httpTransport);
            Optional<Credential> oAuthCredential = getExistingOAuthCredential(oAuthFlow);
            return oAuthCredential.isPresent();
        } catch (IOException e) {
            return false;
        }
    }

    public Optional<Credential> requestTokens(AuthorizationCodeFlow authorizationCodeFlow, String authorizationCode) throws IOException {
        AuthorizationCodeTokenRequest tokenRequest = authorizationCodeFlow.newTokenRequest(authorizationCode);
        TokenResponse tokenResponse = tokenRequest.execute();
        Credential credential = authorizationCodeFlow.createAndStoreCredential(tokenResponse, oauthUserId);
        return Optional.ofNullable(credential);
    }

    private String encode(String str) {
        byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
        return new String(encodedBytes);
    }

}
