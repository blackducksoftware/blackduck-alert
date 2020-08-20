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

import java.net.Proxy;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.gson.Gson;

public class AzureHttpServiceFactory {
    public static final String DEFAULT_BASE_URL = "https://dev.azure.com";
    // TODO consider the tradeoffs of extracting the common base url vs readability
    public static final String DEFAULT_AUTHORIZATION_URL = "https://app.vssps.visualstudio.com/oauth2/authorize?response_type=Assertion";
    public static final String DEFAULT_TOKEN_URL = "https://app.vssps.visualstudio.com/oauth2/token?client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";

    public static AzureHttpService withCredentialNoProxy(Credential oAuthCredential, Gson gson) {
        return withCredentialNoProxy(DEFAULT_BASE_URL, oAuthCredential, gson);
    }

    public static AzureHttpService withCredentialNoProxy(String baseUrl, Credential oAuthCredential, Gson gson) {
        return withCredentialUnauthenticatedProxy(baseUrl, Proxy.NO_PROXY, oAuthCredential, gson);
    }

    public static AzureHttpService withCredentialUnauthenticatedProxy(Proxy proxy, Credential oAuthCredential, Gson gson) {
        return withCredentialUnauthenticatedProxy(DEFAULT_BASE_URL, proxy, oAuthCredential, gson);
    }

    public static AzureHttpService withCredentialUnauthenticatedProxy(String baseUrl, Proxy proxy, Credential oAuthCredential, Gson gson) {
        return withCredential(baseUrl, defaultHttpTransport(proxy), oAuthCredential, gson);
    }

    public static AzureHttpService withCredential(HttpTransport httpTransport, Credential oAuthCredential, Gson gson) {
        return withCredential(DEFAULT_BASE_URL, httpTransport, oAuthCredential, gson);
    }

    public static AzureHttpService withCredential(String baseUrl, HttpTransport httpTransport, Credential oAuthCredential, Gson gson) {
        return new AzureHttpService(baseUrl, httpTransport.createRequestFactory(oAuthCredential), gson);
    }

    private static ApacheHttpTransport defaultHttpTransport(Proxy proxy) {
        // TODO figure out how to use the proxy otherwise remove it and force httpTransport only
        HttpHost httpHost = HttpHost.create(proxy.toString());
        CloseableHttpClient httpClient = ApacheHttpTransport.newDefaultHttpClientBuilder()
                                             .setProxy(httpHost)
                                             .build();
        return new ApacheHttpTransport(httpClient);
    }

}
